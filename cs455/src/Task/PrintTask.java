package Task;

import Worker.Worker;

/**
 * Created by ydubale on 3/2/15.
 */
public class PrintTask implements Task{

    private String rootURL;

    public PrintTask(String rootURL){
        this.rootURL = rootURL;
    }

    @Override
    public void execute(Worker worker) {
        System.out.println("Print task executing: " + rootURL);
        worker.addToTasks(new PrintTask("Haha"));
        System.out.println("Added stuff to task queue");
    }
}
