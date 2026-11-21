package eventforge.service;

/** Outcome of RegistrationService.register, which the controller turns into a message (Design §6.1). */
public enum RegistrationResult {
    CONFIRMED,             // a seat was reserved (UC-5 main flow)
    FULL,                  // no seats left; offer the waitlist (UC-5 alt 3a)
    ALREADY_REGISTERED     // the attendee already holds a registration for this event (UC-5 alt 2a)
}
