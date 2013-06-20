/*
 * Copyright (C) 2013 Joshua Michael Hertlein <jmhertlein@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.jmhertlein.mctowns.remote.auth;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

/**
 *
 * @author joshua
 */
public class AuthenticationChallenge implements Serializable {

    private byte[] challenge;

    public AuthenticationChallenge(int numBytes) {
        challenge = new byte[numBytes];
        new SecureRandom().nextBytes(challenge);
    }

    private AuthenticationChallenge() {
    }

    public AuthenticationChallenge encrypt(Cipher c) throws IllegalBlockSizeException, BadPaddingException {
        AuthenticationChallenge ret = new AuthenticationChallenge();
        ret.challenge = c.doFinal(challenge);

        return ret;
    }

    public AuthenticationChallenge decrypt(Cipher c) throws IllegalBlockSizeException, BadPaddingException {
        AuthenticationChallenge ret = new AuthenticationChallenge();
        ret.challenge = c.doFinal(challenge);

        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Arrays.hashCode(this.challenge);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AuthenticationChallenge other = (AuthenticationChallenge) obj;
        if (!Arrays.equals(this.challenge, other.challenge)) {
            return false;
        }
        return true;
    }
}
