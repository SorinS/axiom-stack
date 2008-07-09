package axiom.objectmodel.dom;


import org.apache.lucene.index.*;
import org.apache.lucene.store.*;

import axiom.extensions.trans.TransactionManager;
import axiom.framework.ErrorReporter;
import axiom.framework.core.Application;
import axiom.objectmodel.db.TransSource;

public class IndexOptimizingRunner implements Runnable {
    
    private Application app;
    private IndexOptimizer optimizer;
    private Directory directory;
    private String indexPath;
    private TransactionManager tmgr;
    private LuceneManager lmgr;
    
    public IndexOptimizingRunner(Directory d, Application app, LuceneManager lmgr) 
    throws Exception {
        this.app = app;
        this.directory = d;
        this.indexPath = d.toString();
        this.optimizer = new IndexOptimizer(d);
        this.optimizer.setUseCompoundFile(true);
        this.optimizer.setMergeFactor(Integer.MAX_VALUE);
        this.tmgr = TransactionManager.newInstance(app.getTransSource());
        this.lmgr = lmgr;
    }
    
    public void run() { 
        TransSource transSource = app.getTransSource();
        
        // keep running the optimizer in a seperate thread, until the server terminates
        while (true) {  
            try {
            	long DEFAULT_SLEEP_VALUE;
            	int DEFAULT_SEGMENT_COUNT;
            	try {
            		DEFAULT_SLEEP_VALUE = Long.parseLong(app.getProperty("optimizer.sleepDuration", "60")) * 1000L;
            	} catch (Exception pe) {
            		DEFAULT_SLEEP_VALUE = 60000L;
            	}
            	try {
            		DEFAULT_SEGMENT_COUNT = Integer.parseInt(app.getProperty("optimizer.segmentThreshold", "10"));
            	} catch (Exception pe) {
            		DEFAULT_SEGMENT_COUNT = 10;
            	}
                Thread.sleep(DEFAULT_SLEEP_VALUE);
                
                int segcount = segmentCount();
                if (segcount < DEFAULT_SEGMENT_COUNT) {
                    continue;
                }
                
                int mergedCount = optimizer.optimize();
                
                if (mergedCount > 1) {
                    tmgr.startTransaction();
                    synchronized (transSource) {
                        optimizer.optimizeSegmentInfos();
                        LuceneManager.commitSegments(tmgr.getConnection(), 
                                tmgr.getApplication(), directory);
                        tmgr.commitTransaction();
                        optimizer.updateIndexUponFinalize();
                        this.lmgr.setSearcherDirty();
                    }
                    optimizer.cleanup();
                } 
            } catch (Exception ex) {
                tmgr.abortTransaction();
                app.logError(ErrorReporter.errorMsg(this.getClass(), "run") 
                		+ "Optimizer for " + indexPath + " encountered a fatal error", ex);
            }
        }
    }
    
    public void runOptimizingThread() {
        Thread t = new Thread(this);
        t.start();
    }
    
    protected int segmentCount() {
        int count = 0;
        SegmentInfos sinfos = IndexObjectsFactory.getFSSegmentInfos(this.directory);
        if (sinfos != null) {
            count = sinfos.size();
        }
        return count;
    }
    
}