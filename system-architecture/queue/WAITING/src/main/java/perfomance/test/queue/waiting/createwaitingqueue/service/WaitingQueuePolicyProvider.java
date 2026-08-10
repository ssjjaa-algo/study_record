package perfomance.test.queue.waiting.createwaitingqueue.service;

import perfomance.test.queue.waiting.createwaitingqueue.domain.WaitingQueuePolicy;

public interface WaitingQueuePolicyProvider {

    WaitingQueuePolicy policyFor(String eventId);
}
