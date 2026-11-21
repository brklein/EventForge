package eventforge.service;

/**
 * Functions shown in the main navigation menu (Design §7). AuthService.permissionsFor(Role) decides which
 * ones a role sees; the services still check roles themselves, so hiding a menu item is not the only guard.
 */
public enum Permission {
    REGISTER_ATTENDEE,     // UC-1
    SEARCH_EDIT_ATTENDEE,  // UC-1
    VIEW_EVENTS,           // UC-2 (Staff: view only)
    CREATE_EVENT,          // UC-2
    MANAGE_ROOMS,          // UC-3
    ASSIGN_ROOM,           // UC-4
    CHECK_IN,              // UC-7
    GENERATE_REPORT        // UC-8
}
