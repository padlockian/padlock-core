/*
* Copyright (c) 2009-2012 Jason Nichols

* Permission is hereby granted, free of charge, to any person obtaining a copy 
* of this software and associated documentation files (the "Software"), to deal 
* in the Software without restriction, including without limitation the rights to 
* use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies 
* of the Software, and to permit persons to whom the Software is furnished to do 
* so, subject to the following conditions:

* The above copyright notice and this permission notice shall be included in all 
* copies or substantial portions of the Software.

* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
* SOFTWARE.
*/

package net.padlocksoftware.padlock.validator.history;

import java.util.Date;

/**
 * This interface is used internally by the validator, and should not be
 * instantiated outside of the Padlock library.
 *
 * @author Jason Nichols (jason@padlocksoftware.net)
 */
public interface History {

    /**
     * Get the earliest known run for the supplied sig.
     * @param sig The key signature to search for.  This key should have the
     * trailing '=' stripped for compatibility.
     * @return The earliest known run date for a license matching the supplied
     * sig, or null if none is found.
     */
    public Date getEarliestRun(String sig);

    /**
     * Set the earliest run date for a given key signature
     * @param sig The key signature to set the earliest run.  This signature
     * should have the trailing '=' stripped off for compatibility.
     * @param date The date of the earlies known sig use.
     */
    public void setEarliestRun(String sig, Date date);
}
