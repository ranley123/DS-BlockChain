import java.util.concurrent.PriorityBlockingQueue;

public class WorkPuzzle {
    PriorityBlockingQueue<BlockRecord> queue = null;
    public WorkPuzzle(PriorityBlockingQueue<BlockRecord> blockQueue){
        queue = blockQueue;
    }

    public void work(){
        while(!queue.isEmpty()){
            BlockRecord block = queue.poll();

            // check is not already in the verified block chain
            if(Blockchain.hasDuplicateBlock(block)){
                continue;
            }

        }
    }
}
