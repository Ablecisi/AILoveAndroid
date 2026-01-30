package com.ailianlian.ablecisi.baseclass;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * ailianlian
 * com.ailianlian.ablecisi.baseclass
 * BaseRepository <br>
 * 数据仓库基类
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/6/15
 * 星期日
 * 23:29
 */
public class BaseRepository {
    private final ExecutorService executorService;

    private Context context;

    public BaseRepository(Context context) {
        this.context = context;
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * 获取应用上下文
     *
     * @return 应用上下文
     */
    public Context getContext() {
        return context;
    }

    /**
     * 获取单线程执行器
     *
     * @return 单线程执行器
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * 数据回调接口
     *
     * @param <T> 数据类型
     */
    public interface DataCallback<T> {
        void onSuccess(T data);

        void onError(String msg);

        void onNetworkError();
    }

    /**
     * 关闭执行器
     */
    public void shutdown() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }

        if (!executorService.isTerminated()) {
            try {
                boolean awaitTermination = executorService.awaitTermination(60, TimeUnit.SECONDS);
                if (!awaitTermination) {
                    executorService.shutdownNow(); // 强制关闭
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (context != null) {
            context = null; // 清理上下文引用
        }
        System.out.println("BaseRepository: ExecutorService has been shut down and context cleared.");
    }

}
