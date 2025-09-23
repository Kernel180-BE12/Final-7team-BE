// package com.softlabs.aicontents.domain.scheduler.service;
//
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.annotation.Bean;
// import org.springframework.scheduling.TaskScheduler;
// import org.springframework.scheduling.annotation.EnableScheduling;
// import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
// import org.springframework.scheduling.support.CronTrigger;
// import org.springframework.stereotype.Service;
//
// import java.time.LocalDateTime;
// import java.time.ZoneId;
// import java.time.format.DateTimeFormatter;
// import java.util.Map;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.ScheduledFuture;
//
// @Slf4j
// @Service
// @EnableScheduling
// public class DynamicSchedulerService {
//
//    @Autowired
//    private TaskScheduler taskScheduler;
//
//    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
//
//    @Bean
//    public TaskScheduler taskScheduler() {
//        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
//        scheduler.setPoolSize(10);
//        scheduler.setThreadNamePrefix("dynamic-scheduler-");
//        scheduler.initialize();
//        return scheduler;
//    }
//
//    public void scheduleTask(String taskId, String cronExpression, Runnable task) {
//        try {
//            stopTask(taskId);
//
//            CronTrigger cronTrigger = new CronTrigger(cronExpression);
//            ScheduledFuture<?> scheduledTask = taskScheduler.schedule(task, cronTrigger);
//
//            scheduledTasks.put(taskId, scheduledTask);
//            log.info("Task scheduled successfully. TaskId: {}, Cron: {}", taskId, cronExpression);
//
//        } catch (Exception e) {
//            log.error("Failed to schedule task. TaskId: {}, Cron: {}", taskId, cronExpression, e);
//            throw new RuntimeException("Failed to schedule task: " + e.getMessage(), e);
//        }
//    }
//
//    public void stopTask(String taskId) {
//        ScheduledFuture<?> scheduledTask = scheduledTasks.get(taskId);
//        if (scheduledTask != null) {
//            scheduledTask.cancel(true);
//            scheduledTasks.remove(taskId);
//            log.info("Task stopped successfully. TaskId: {}", taskId);
//        }
//    }
//
//    public boolean isTaskRunning(String taskId) {
//        ScheduledFuture<?> scheduledTask = scheduledTasks.get(taskId);
//        return scheduledTask != null && !scheduledTask.isCancelled() && !scheduledTask.isDone();
//    }
//
//    public String convertTimeToCron(String executionTime) {
//        try {
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//            LocalDateTime dateTime = LocalDateTime.parse(executionTime, formatter);
//
//            int second = dateTime.getSecond();
//            int minute = dateTime.getMinute();
//            int hour = dateTime.getHour();
//            int day = dateTime.getDayOfMonth();
//            int month = dateTime.getMonthValue();
//
//            return String.format("%d %d %d %d %d *", second, minute, hour, day, month);
//
//        } catch (Exception e) {
//            log.error("Failed to convert time to cron expression: {}", executionTime, e);
//            throw new IllegalArgumentException("Invalid execution time format: " + executionTime,
// e);
//        }
//    }
//
//    public void stopAllTasks() {
//        scheduledTasks.forEach((taskId, scheduledTask) -> {
//            scheduledTask.cancel(true);
//            log.info("Task stopped: {}", taskId);
//        });
//        scheduledTasks.clear();
//        log.info("All scheduled tasks stopped");
//    }
//
//    public int getActiveTaskCount() {
//        return (int) scheduledTasks.values().stream()
//                .filter(task -> !task.isCancelled() && !task.isDone())
//                .count();
//    }
// }
