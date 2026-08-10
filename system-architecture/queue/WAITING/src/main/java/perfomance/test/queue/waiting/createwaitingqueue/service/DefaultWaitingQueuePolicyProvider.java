package perfomance.test.queue.waiting.createwaitingqueue.service;

import perfomance.test.queue.waiting.config.WaitingQueueProperties;
import perfomance.test.queue.waiting.createwaitingqueue.domain.WaitingQueuePolicy;

import org.springframework.stereotype.Component;

@Component
public class DefaultWaitingQueuePolicyProvider implements WaitingQueuePolicyProvider {

    private final WaitingQueueProperties properties;

    public DefaultWaitingQueuePolicyProvider(WaitingQueueProperties properties) {
        this.properties = properties;
    }

    @Override
    public WaitingQueuePolicy policyFor(String eventId) {
        WaitingQueueProperties.Defaults defaults = properties.defaults();
        return new WaitingQueuePolicy(
                defaults.maxWaitingUsers(),
                defaults.maxActiveUsers(),
                defaults.admissionRatePerSecond(),
                defaults.waitingInactivityTimeoutSeconds(),
                defaults.activeInactivityTimeoutSeconds(),
                defaults.maxActiveDurationSeconds(),
                defaults.dataRetentionSeconds()
        );
    }
}
