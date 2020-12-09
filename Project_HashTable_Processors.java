
package project_hashtable_processors;

import com.sun.org.apache.xpath.internal.operations.Equals;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Date;

/**
 *
 * @author Matthew Meeh
 * @date 11/30/2020
 */
public class Project_HashTable_Processors {
    
    /**
     * @param args 
     * /gamma = the number of insertions into the program
     * list of /mew = amount of processor time for each insertion
     * number of processors
     * list of processor speed
     * flags
     */
    //List's look like
    //1,2,3
    
    public MultipleIntegers[] HashMapMatches;//each int points to either the Task array or Processor array, made when a Task is done by a processor
    //in combination with the two other arrays, this creates a HashMap.
    
    public static void main(String[] args) {
        Project_HashTable_Processors program = new Project_HashTable_Processors(args);
    }
    //DONE (UNLESS I CHOOSE TO USE FILE INSTEAD)
    public Project_HashTable_Processors(String[] args){// argument handling
        long currentTime = System.currentTimeMillis();
        Integer tempInt = new Integer(args[0]);
        int numberOfInsertions = tempInt.intValue();
        String strList = args[1]; String tempStr = "";
        LinkedList<Task> listInsertion = new LinkedList<>();
        for(int i = 0; i < strList.length(); i++){
           if(i == strList.length()-1){
               tempStr = tempStr + strList.charAt(i);
               tempInt = Integer.parseInt(tempStr);
               Task tempTask = new Task(tempInt.intValue());
               listInsertion.add(tempTask);
           }
           if(strList.charAt(i) == ',' ){
               tempInt = Integer.parseInt(tempStr);
               Task tempTask = new Task(tempInt.intValue());
               listInsertion.add(tempTask);
               tempStr = "";
           }
           else {
               tempStr = tempStr + strList.charAt(i);
           }
        }
        tempStr = "";
        tempInt = Integer.parseInt(args[2]);
        int numberOfProcessors = tempInt.intValue();
        strList = args[3];
        LinkedList<Processor> listProcessor = new LinkedList<>();
        int id_processor = 0;
        for(int i = 0; i < strList.length(); i++){
           if(i == strList.length()-1){
               tempStr = tempStr + strList.charAt(i);
               tempInt = Integer.parseInt(tempStr);
               Processor tempProcessor = new Processor(id_processor, tempInt.intValue());
               listProcessor.add(tempProcessor);
           }
           if(strList.charAt(i) == ','){
               tempInt = Integer.parseInt(tempStr);
               Processor tempProcessor = new Processor(id_processor, tempInt.intValue());
               listProcessor.add(tempProcessor);
               tempStr = ""; id_processor++;
           }
           else {
               tempStr = tempStr + strList.charAt(i);
           }        }
        tempInt = Integer.parseInt(args[4]);
        int flag = tempInt.intValue();
        // 2 includes all possible outputs (5 and 6 inaddition to 1's)
        // 1 includes 1,2,3,4
        // 0 includes all sub-sections (statistics only)
        // -1 is no print
        Task[] arrayInsertion = new Task[listInsertion.size()];
        listInsertion.toArray(arrayInsertion);
        Processor[] arrayProcessor = new Processor[listProcessor.size()];
        listProcessor.toArray(arrayProcessor);
        HashMapMatches = new MultipleIntegers[numberOfInsertions];
        // activate the 'program'
        Algorithm start = new Algorithm(numberOfInsertions, arrayInsertion, numberOfProcessors, arrayProcessor);
        Determine values = new Determine(numberOfInsertions, arrayProcessor, arrayInsertion);//getting stats
        long passedTime = System.currentTimeMillis();
        //print stuff
        // wait things to be printed
        if(flag >= 1){
            for(int indexWait = 0; indexWait < values.waitTimes.length; indexWait++){
                System.out.println("The Task " + indexWait + " has a wait time of " + values.waitTimes[indexWait]);
            }
        }
        if(flag >= 0){
            System.out.println("The average wait time for any Task is " + values.avgWaitTime);
            System.out.println("The longest wait time is " + values.longestWaitTime);
        }
        // deletion things to be printed
        if(flag >= 1){
            for(int indexDeletion = 0; indexDeletion < values.waitTimes.length; indexDeletion++){
                System.out.println("The Task " + indexDeletion + " has a service time of " + values.deletionTimes[indexDeletion]);
            }
        }
        if(flag >= 0){
            System.out.println("The average deletion (service) time for any Task is " + values.avgDeletionTime);
            System.out.println("The longest deletion (service) time is " + values.longestDeletionTime);
            System.out.println("The shorstest deletion (service) time is " + values.shortestDeletionTime);
        }
        // each processor's queue
        if(flag >= 1){
            for(int indexLength = 0; indexLength < values.queueLengths.length; indexLength++){
                System.out.println("The processor " + indexLength + " had a queue of " + values.queueLengths[indexLength]);
            }
        }
        if(flag >= 0){
            System.out.println("The average length of any queue is " + values.avgProcessorQueue);
            System.out.println("The longest queue is size of " + values.longestQueueLength);
            System.out.println("The shortest queue is size of " + values.shortestQueueLength);
        }
        if(flag == 2){
            System.out.println("The \"fake\" time is " + values.expectedFakeTime);
            long timeDiff = passedTime - currentTime;
            System.out.println("The real time is " + timeDiff);
            double relationship = (double) timeDiff/values.expectedFakeTime;
            System.out.println("The relationship between the two is" + relationship);
        }
        // ?? consider printing out each queue
        
    }
    public class Algorithm{//main program
        //does each insertions using every available processor
        Queue<Task>[] queues;// ?? maybe use with printing each queue
        
        public Algorithm(int numInsetions, Task[] insertionList, int numProcessors, Processor[] speedList){
            for(int indexInsertion = 0; indexInsertion < insertionList.length; indexInsertion++){
                Task Task = insertionList[indexInsertion];
                if(Task.absoluteServiceTime <= 0){
                    continue; // instanteous
                }
                MultipleIntegers toWaitOrNot = notFull(speedList, insertionList[indexInsertion]);
                if(toWaitOrNot.firstData != -1){
                    MultipleIntegers returnedObject = findOptimalProcessor(speedList,insertionList[indexInsertion]);
                    MultipleIntegers instance = new MultipleIntegers();
                    instance.firstData = indexInsertion;
                    instance.secondData = returnedObject.secondData;
                    HashMapMatches[indexInsertion] = instance;
                    if(returnedObject.firstData != 0){//is not done instanteous
                        //
                        occupyProcessor(speedList[returnedObject.secondData]);
                        double serviceTime = calculateRatio(insertionList[indexInsertion].getAbsServiceTime(),
                                speedList[returnedObject.secondData].getSpeed());
                        int roundedTime = roundUp(serviceTime);
                        try {
                            insertionList[indexInsertion].setRelServiceTime(roundedTime);
                        }
                        catch (DataLoss exception){
                            System.err.println("ERROR HAS OCCURED" + exception);
                            System.exit(1);
                        }
                        
                    }
                    else {// done as instantly 
                        try {
                            insertionList[indexInsertion].setRelServiceTime(1);
                        }
                        catch (DataLoss exception){
                            System.err.println("ERROR HAS OCCURED" + exception);
                            System.exit(1);
                        }
                    } 
                }
                else {//need to wait
                    // change waitingTime TODO
                    
                    int waitTime = speedList[toWaitOrNot.secondData].getSpeed(); //change??
                    SpecialWait(waitTime);
                    // 
                    popAllProcessors(waitTime, speedList, insertionList); //Guantreed to pop speedlist[toWaitOrNot.secondData]
                    try {
                        insertionList[indexInsertion].setWaitingTime(waitTime);
                    }
                    catch (DataLoss exception) {
                        System.err.println("ERROR HAS OCCURED" + exception);
                        System.exit(2);
                    }
                    indexInsertion--; //didn't do the assigned Task
                }
            }
        }
    }
    // end of class
    //DONE
    public void SpecialWait(int time){
        for(int i = 0; i < time; i++){}
    }
    //DONE
    public MultipleIntegers notFull(Processor[] list, Task task){// NOTE: This does NOT find an optimal processor if it does not exist
        MultipleIntegers data = new MultipleIntegers();
        //  first data is if there is no free processors, second data is the optimal processor to wait on
        // First: -1 means no free processors, 1 means at least 1 free processor
        // Second: is the index of the processor, must be a POSITIVE INTEGER
        double ratio = Double.MAX_VALUE;
        data.firstData = -1;
        for(int indexProcessor = 0; indexProcessor < list.length; indexProcessor++){
            if(list[indexProcessor].occupied == false){
                data.firstData = 1;
                data.secondData = indexProcessor;
                break;//find first open processor, don't need to search anymore
            }
            else {//**this guantrees a processor that is optimal if none are available**
                double currRatio = calculateRatio(task.getAbsServiceTime(), list[indexProcessor].getSpeed());
                if(ratio > currRatio){
                    ratio = currRatio;
                    data.secondData = indexProcessor;
                }
                else {
                    //Ignore that processor as it's not useful to wait on
                }
            }
        }
        return data;
    }
    //DONE
    public double calculateRatio(int numerator, int denomatior){
        double tempNum = (double) numerator;
        double tempDen = (double) denomatior;
        double result = tempNum/tempDen;
        return result;
    }
    //DONE
    public MultipleIntegers findOptimalProcessor(Processor[] list, Task task){
        MultipleIntegers data = new MultipleIntegers();
        // first data is if the processor can do the task instantly, second data is the index of the processor
        // First: 0 means the task is done instantly otherwise it is the amount of time the processor will take
        // Second: index of processor, must be a POSITIVE INTEGER
        double ratio = Double.MAX_VALUE;
        for(int indexProcessor = 0; indexProcessor < list.length; indexProcessor++){
            if(list[indexProcessor].occupied == false){
                double currRatio = calculateRatio(task.getAbsServiceTime(), list[indexProcessor].getSpeed());
                if(ratio > currRatio){
                    ratio = currRatio;
                    data.secondData = indexProcessor;
                }
                else {
                    continue;
                    //Ignore that processor as it's not useful to wait on
                }
                if(currRatio <= 1.0){
                    data.firstData = 0;
                }
                else {
                    int ratioRounded = roundUp(currRatio);
                    data.firstData = ratioRounded;
                }
            }
            else {} //if it's occupied don't bother with it
        }//will go look at ALL processors
        return data;
    }
    public int roundUp(double d){
        int i = (int) d;
        i = i + 1;
        return i;
    }
    //DONE
    public void occupyProcessor(Processor processor){
       processor.setOccupied(true);
    }
    //DONE
    public void popAllProcessors(int time, Processor[] listProcessors, Task[] listTasks){
        for(int indexMatch = 0; indexMatch < listProcessors.length; indexMatch++){
            //if(TaskProcessorMatches[indexMatch] == null){
            //    break; // reached effective end of array
            //}
            //else {
                Processor refProcessor = listProcessors[HashMapMatches[indexMatch].firstData];
                Task refTask = listTasks[HashMapMatches[indexMatch].secondData];
                if(refProcessor.isOccupied()){
                    double ratio = calculateRatio(refTask.getAbsServiceTime(), refProcessor.getSpeed());
                    int rounded = roundUp(ratio);
                    if(rounded < time){
                        refProcessor.setOccupied(false);
                    }
                    else {
                        //processor still going
                    }
                }
                else {
                    // processor is already been freed
                }
           // }
        }
    }
    //DONE
    public class Task{
        private int absoluteServiceTime; // running time
        private SpecialTime relativeSerciceTime; // service time
        private SpecialTime waitingTime;
        
        public Task(int integer){
            absoluteServiceTime = integer;
        }
        //GETTERS
        public int getAbsServiceTime(){
            return absoluteServiceTime;
        }
        public int getWaitingTime(){
            if(waitingTime == null){
                waitingTime = new SpecialTime(0);
            }
            return waitingTime.dereference;
        }
        public int getRelServiceTime(){
            if(relativeSerciceTime == null){
                relativeSerciceTime = new SpecialTime(absoluteServiceTime);
            } 
            return relativeSerciceTime.dereference;
        }
        //SETTERS
        public void setWaitingTime(int time) throws DataLoss{
            if(waitingTime != null){
                System.err.println("WARNING: Setting waiting time to something else while it contains a value.");
                DataLoss data = new DataLoss(waitingTime);
                throw data;
            }
            waitingTime = new SpecialTime(time);
        }
        public void setRelServiceTime(int time) throws DataLoss{
            if(relativeSerciceTime != null){
                System.err.println("WARNING: Setting waiting time to something else while it contains a value.");
                DataLoss data = new DataLoss(relativeSerciceTime);
                throw data;
            }
            relativeSerciceTime = new SpecialTime(time);  
        }
    }
    //end of class Task (insertion)
    //DONE MAYBE?
    public class DataLoss extends Exception{
        public DataLoss(Object data){
            System.err.println("We loss some data" + data);
        }
    }//end of special throwing class
    //DONE
    public class SpecialTime{
        public int dereference;
        
        public SpecialTime(int integer){
            dereference = integer;
        }
    }
    //NOT DONE AT ALL
    public class Determine{//determines statistics from the dataset
        int[] waitTimes; // 1
        double avgWaitTime = -1; // 1a
        int longestWaitTime = -1; // 1b
        int[] deletionTimes; // 3
        double avgDeletionTime = -1; // 3a
        int shortestDeletionTime = -1; // 3b
        int longestDeletionTime = -1; // 3c
        int[] queueLengths; // 4
        double avgProcessorQueue = -1; // 4a
        int shortestQueueLength = -1; // 4b
        int longestQueueLength = -1; // 4c
        /// 2, 5 and 6 are determined elsewhere
        int expectedFakeTime = -1; // should match the expected fake time (6)
        //figure out the relation between fake and real time elsewhere
        
        public Determine(int numTasks, Processor[] processors, Task[] tasks){
            //Setting up variables
            waitTimes = new int[numTasks];
            deletionTimes = new int[numTasks];
            queueLengths = new int[processors.length];
            int totalServiceTime = 0;
            int totalSpeed = 0;
            int totalWaitTime = 0;
            longestWaitTime = -1;
            int totalDeletionTime = 0;
            longestDeletionTime = -1;
            shortestDeletionTime = Integer.MAX_VALUE;
            avgProcessorQueue = (double) numTasks/processors.length;
            longestQueueLength = -1;
            shortestQueueLength = Integer.MAX_VALUE;
            //Done with variables
            //Getting info from processors
            for(int indexProcessor = 0; indexProcessor < processors.length; indexProcessor++){
                totalSpeed = totalSpeed + processors[indexProcessor].getSpeed();
                queueLengths[indexProcessor] = 0;
            }
            //Getting Task info
            for(int indexMatch = 0; indexMatch < HashMapMatches.length; indexMatch++){
                MultipleIntegers tempIndexes = HashMapMatches[indexMatch];
                Processor refProcessor = processors[tempIndexes.secondData];
                Task refTask = tasks[tempIndexes.firstData];
                totalServiceTime = totalServiceTime + refTask.getAbsServiceTime();
                // 1
                waitTimes[indexMatch] = refTask.getWaitingTime();
                totalWaitTime = totalWaitTime + refTask.getWaitingTime();
                if(longestWaitTime < refTask.getWaitingTime()){
                    longestWaitTime = refTask.getWaitingTime();
                }
                // 3
                deletionTimes[indexMatch] = refTask.getRelServiceTime();
                totalDeletionTime = totalDeletionTime + refTask.getRelServiceTime();
                if(longestDeletionTime < refTask.getRelServiceTime()){
                    longestDeletionTime = refTask.getRelServiceTime();
                }
                if(shortestDeletionTime > refTask.getRelServiceTime()){
                    shortestDeletionTime = refTask.getRelServiceTime();
                }
                // 4
                queueLengths[findProcessorIndex(refProcessor, processors)]++;
                
            }
            //Combining them
            for(int indexProcessor = 0; indexProcessor < processors.length; indexProcessor++){
                int temp = queueLengths[indexProcessor];
                if(shortestQueueLength > temp){
                    shortestQueueLength = temp;
                }
                if(longestQueueLength < temp){
                    longestQueueLength = temp;
                }
            }
            //doing averages
            avgDeletionTime = (double) totalDeletionTime/numTasks;
            avgWaitTime = (double) totalWaitTime/numTasks;
            expectedFakeTime = (int) totalServiceTime/totalSpeed;
        }
        //Finsihed getting stats
    }
    public int findProcessorIndex(Processor process, Processor[] list){
        for(int indexProcessor = 0; indexProcessor < list.length; indexProcessor++){
            if(process.equals(list[indexProcessor])){
                return indexProcessor;// Found it!
            }
            else {
                //Not the processor we are looking for
            }
        }
        return -1; //error, could not find the processor
    }
    //DONE
    public class MultipleIntegers{//for saving multiple integers without creating an array (for small number of integers)
        public int firstData;
        public int secondData; 
        
        public MultipleIntegers(){      }
    }
    //DONE
    public class Processor{// for the processors 
        private boolean occupied;
        private int speed;
        public int identifier;
        
        public Processor(int id, int power){
            identifier = id;
            speed = power;
            occupied = false;
        }
        //GETTERS
        public int getSpeed(){
            return speed;
        }
        public boolean isOccupied(){
            return occupied;
        }
        //SETTER
        public void setOccupied(boolean bool){
            occupied = bool;
        }


        public boolean equals(Processor processor){
            if(this.identifier == processor.identifier){
                return true;
            }
            return false;
        }
    }
}
