package im.vinci.server.other.domain.user;

import im.vinci.server.common.exceptions.EnumOutOfBoundException;

/**
 * Created by henryhome on 3/9/15.
 */
public enum UserRole {

    ANYBODY,
    ADMIN,
    NORMAL_USER;

    public static UserRole [] all = UserRole.values();
    public static UserRole fromInt(Integer n) throws EnumOutOfBoundException {
        if (n == null) {
            return null;
        } else if (n >= 0 && n < all.length) {
            return all[n];
        } else {
            throw new EnumOutOfBoundException("the integer " + n + " is beyond the range of 'UserRole' enum type");
        }
    }    
    public static UserRole fromName(String name) throws EnumOutOfBoundException {
        if (name == null) {
            return null;
        } 

        for (UserRole userRole : all){
        	if (userRole.name().equals(name))
        		return userRole;
        }

        throw new EnumOutOfBoundException("the integer " + name + " is beyond the range of 'UserRole' enum type");
    }
}



