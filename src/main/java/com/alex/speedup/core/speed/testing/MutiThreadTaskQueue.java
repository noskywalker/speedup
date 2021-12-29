package com.alex.speedup.core.speed.testing;

import org.testng.IHookCallBack;
import org.testng.ITestResult;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * xianshuangzhang@gmail.com
 */
public class MutiThreadTaskQueue {
    BlockingQueue<MutiThreadTaskQueue.TaskResult> queue = new LinkedBlockingQueue();

    public void add(MutiThreadTaskQueue.TaskResult taskResult) {
        this.queue.add(taskResult);
    }

    public MutiThreadTaskQueue.TaskResult poll() {
        try {
            return (MutiThreadTaskQueue.TaskResult)this.queue.take();
        } catch (InterruptedException var2) {
            throw new RuntimeException(var2);
        }
    }

    public MutiThreadTaskQueue() {
    }

    public BlockingQueue<MutiThreadTaskQueue.TaskResult> getQueue() {
        return this.queue;
    }

    public void setQueue(BlockingQueue<MutiThreadTaskQueue.TaskResult> queue) {
        this.queue = queue;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof MutiThreadTaskQueue)) {
            return false;
        } else {
            MutiThreadTaskQueue other = (MutiThreadTaskQueue)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$queue = this.getQueue();
                Object other$queue = other.getQueue();
                if (this$queue == null) {
                    if (other$queue != null) {
                        return false;
                    }
                } else if (!this$queue.equals(other$queue)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof MutiThreadTaskQueue;
    }

    public int hashCode() {
        int result = 1;
        Object $queue = this.getQueue();
        result = result * 59 + ($queue == null ? 43 : $queue.hashCode());
        return result;
    }

    public String toString() {
        return "MutiThreadTaskQueue(queue=" + this.getQueue() + ")";
    }

    public static class TaskResult implements Serializable {
        IHookCallBack callBack;
        ITestResult testResult;
        boolean success;
        String message;

        public TaskResult(IHookCallBack callBack, ITestResult testResult, boolean success, String message) {
            this.callBack = callBack;
            this.testResult = testResult;
            this.success = success;
            this.message = message;
        }

        public IHookCallBack getCallBack() {
            return this.callBack;
        }

        public ITestResult getTestResult() {
            return this.testResult;
        }

        public boolean isSuccess() {
            return this.success;
        }

        public String getMessage() {
            return this.message;
        }

        public void setCallBack(IHookCallBack callBack) {
            this.callBack = callBack;
        }

        public void setTestResult(ITestResult testResult) {
            this.testResult = testResult;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof MutiThreadTaskQueue.TaskResult)) {
                return false;
            } else {
                MutiThreadTaskQueue.TaskResult other = (MutiThreadTaskQueue.TaskResult)o;
                if (!other.canEqual(this)) {
                    return false;
                } else {
                    Object this$callBack = this.getCallBack();
                    Object other$callBack = other.getCallBack();
                    if (this$callBack == null) {
                        if (other$callBack != null) {
                            return false;
                        }
                    } else if (!this$callBack.equals(other$callBack)) {
                        return false;
                    }

                    Object this$testResult = this.getTestResult();
                    Object other$testResult = other.getTestResult();
                    if (this$testResult == null) {
                        if (other$testResult != null) {
                            return false;
                        }
                    } else if (!this$testResult.equals(other$testResult)) {
                        return false;
                    }

                    if (this.isSuccess() != other.isSuccess()) {
                        return false;
                    } else {
                        Object this$message = this.getMessage();
                        Object other$message = other.getMessage();
                        if (this$message == null) {
                            if (other$message != null) {
                                return false;
                            }
                        } else if (!this$message.equals(other$message)) {
                            return false;
                        }

                        return true;
                    }
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof MutiThreadTaskQueue.TaskResult;
        }

        public int hashCode() {
            int result = 1;
            Object $callBack = this.getCallBack();
            result = result * 59 + ($callBack == null ? 43 : $callBack.hashCode());
            Object $testResult = this.getTestResult();
            result = result * 59 + ($testResult == null ? 43 : $testResult.hashCode());
            result = result * 59 + (this.isSuccess() ? 79 : 97);
            Object $message = this.getMessage();
            result = result * 59 + ($message == null ? 43 : $message.hashCode());
            return result;
        }

        public String toString() {
            return "MutiThreadTaskQueue.TaskResult(callBack=" + this.getCallBack() + ", testResult=" + this.getTestResult() + ", success=" + this.isSuccess() + ", message=" + this.getMessage() + ")";
        }
    }
}
