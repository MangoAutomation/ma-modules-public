package com.serotonin.m2m2.internal.threads;

import java.lang.Thread.State;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.module.DwrDefinition;
import com.serotonin.m2m2.web.dwr.ModuleDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;
import com.serotonin.monitor.ValueMonitor;

public class ThreadsDwrDef extends DwrDefinition {
    @Override
    public Class<? extends ModuleDwr> getDwrClass() {
        return ThreadsDwr.class;
    }

    public static class ThreadsDwr extends ModuleDwr {
        private final ThreadMXBean tmxb = ManagementFactory.getThreadMXBean();

        private final Map<Long, ThreadInfoBean> threadInfos = new HashMap<>();

        @DwrPermission(admin = true)
        public ProcessResult getStatusVars() {
            ProcessResult result = new ProcessResult();

            Map<String, Object> internal = new LinkedHashMap<>();
            result.addData(translate("internal.status"), internal);
            for (ValueMonitor<?> monitor : Common.MONITORED_VALUES.getMonitors())
                internal.put(translate(monitor.getName()), monitor.getValue());

            //Common.databaseProxy.addStatusValues(result, getTranslations());

            return result;
        }

        @DwrPermission(admin = true)
        public ProcessResult getThreadInfo() {
            synchronized (threadInfos) {
                ProcessResult result = new ProcessResult();

                // All of the last thread ids. Ids are removed from this set as they are processed. If ids remain,
                // it means the thread is gone and should be removed from the map.
                Set<Long> threadIds = new HashSet<>(threadInfos.keySet());

                ThreadInfo[] threads = tmxb.getThreadInfo(tmxb.getAllThreadIds(), Integer.MAX_VALUE);
                List<ThreadInfoBean> beans = new ArrayList<>();
                for (ThreadInfo thread : threads) {
                    if (thread == null)
                        continue;

                    ThreadInfoBean bean = threadInfos.get(thread.getThreadId());
                    if (bean == null) {
                        bean = new ThreadInfoBean();
                        bean.setId(thread.getThreadId());
                        bean.setName(thread.getThreadName());
                        threadInfos.put(bean.getId(), bean);
                    }
                    else
                        threadIds.remove(bean.getId());

                    bean.setCpuTime(tmxb.getThreadCpuTime(bean.getId()));
                    bean.setState(thread.getThreadState().name());

                    if (thread.getThreadState() == State.BLOCKED)
                        bean.setState(bean.getState() + " by '" + thread.getLockOwnerName() + "' ("
                                + thread.getLockOwnerId() + ")");

                    bean.setStackTrace(thread.getStackTrace());

                    beans.add(bean);
                }

                // Remove unreferenced threads
                for (Long id : threadIds)
                    threadInfos.remove(id);

                result.addData("threads", beans);

                return result;
            }
        }

        @DwrPermission(admin = true)
        public ProcessResult getWorkItems() {
            ProcessResult result = new ProcessResult();

            result.addData("medClassCounts", Common.backgroundProcessing.getMediumPriorityServiceQueueClassCounts());
            result.addData("lowClassCounts", Common.backgroundProcessing.getLowPriorityServiceQueueClassCounts());

            return result;
        }
    }
}
