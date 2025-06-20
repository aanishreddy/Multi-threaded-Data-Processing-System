
package main

import (
    "fmt"
    "math/rand"
    "sync"
    "time"
)

// Task represents a unit of work
type Task struct {
    ID int
}

// SharedQueue is a thread-safe queue for tasks
type SharedQueue struct {
    tasks []Task
    lock  sync.Mutex
}

// AddTask adds a task to the queue
func (q *SharedQueue) AddTask(task Task) {
    q.lock.Lock()
    defer q.lock.Unlock()
    q.tasks = append(q.tasks, task)
}

// GetTask retrieves and removes a task from the queue
func (q *SharedQueue) GetTask() (Task, bool) {
    q.lock.Lock()
    defer q.lock.Unlock()
    if len(q.tasks) == 0 {
        return Task{}, false
    }
    task := q.tasks[0]
    q.tasks = q.tasks[1:]
    return task, true
}

var (
    resultLock sync.Mutex
    results    []string
)

func worker(id int, queue *SharedQueue, wg *sync.WaitGroup) {
    defer wg.Done()
    for {
        task, ok := queue.GetTask()
        if !ok {
            return
        }
        fmt.Printf("Worker %d processing task %d\n", id, task.ID)
        time.Sleep(time.Duration(rand.Intn(1000)) * time.Millisecond) // Simulate work

        result := fmt.Sprintf("Worker %d completed task %d", id, task.ID)
        resultLock.Lock()
        results = append(results, result)
        resultLock.Unlock()
    }
}

func main() {
    rand.Seed(time.Now().UnixNano())
    taskQueue := &SharedQueue{}
    for i := 1; i <= 20; i++ {
        taskQueue.AddTask(Task{ID: i})
    }

    var wg sync.WaitGroup
    numWorkers := 5

    for i := 1; i <= numWorkers; i++ {
        wg.Add(1)
        go worker(i, taskQueue, &wg)
    }

    wg.Wait()

    fmt.Println("\nAll tasks completed. Results:")
    for _, result := range results {
        fmt.Println(result)
    }
}
