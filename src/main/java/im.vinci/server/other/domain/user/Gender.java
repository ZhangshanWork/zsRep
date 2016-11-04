package im.vinci.server.other.domain.user;

import im.vinci.server.common.exceptions.EnumOutOfBoundException;

/**
 * Created by henryhome on 9/11/15.
 */
public enum Gender {

    UNKNOWN,
    MALE,
    FEMALE,
    NEUTRAL;

    public static Gender [] all = Gender.values();
    public static Gender fromInt(Integer n) throws EnumOutOfBoundException {
        if (n == null) {
            return null;
        } else if (n >= 0 && n < all.length) {
            return all[n];
        } else {
            throw new EnumOutOfBoundException("the integer " + n + " is beyond the range of 'Gender' enum type");
        }
    }

    public static Gender fromName(String name) throws EnumOutOfBoundException {
        if (name == null) {
            return null;
        }

        for (Gender gender : all) {
            if (gender.name().equals(name))
                return gender;
        }

        throw new EnumOutOfBoundException("the name " + name + " is beyond the range of 'Gender' enum type");

    }
}
