 Readme


 # ThreadPool

 This is my implementation of thread pool based on the executor service framework. 
 
 ThreadPool is used to reduce the overhead needed to create new threads and add another abstraction layer so that the user doesn't have to deal with creating \ managing threads. 
 
Built on top of a waitable priority queue.


## Methods

  # Submit
  Method to create the new task with its priority and push it to the queue. 

  # SetNumOfThread
  Method that used to control the number of active threads.
  
  # Shutdown
  Terminate the thread pool and stop all the threads after completion of all the tasks in queue. 
  
  # awaitTermination 
  Terminate the thread pool after the current task of each thread has completed.
