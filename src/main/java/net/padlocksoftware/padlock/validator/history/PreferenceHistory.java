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
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 * This class is used internally by the validator, and should not be
 * instantiated outside of the Padlock library.
 *
 * @author Jason Nichols(jason@padlocksoftware.net)
 */
final public class PreferenceHistory implements History {

  Set<Preferences> prefSet = new HashSet<Preferences>();

  public PreferenceHistory() {

    //
    // Let's try out two preference locations:
    //
    prefSet.add(Preferences.userRoot().node("com/nto/info"));
    //prefSet.add(Preferences.systemRoot().node("com/nto/info"));

  }

  public Date getEarliestRun(String sig) {
    //
    // Java preferences only supports key lengths up to 80 characters.
    // We'll go with the first 64 to give us room to grow.
    //
    sig = sig.substring(0, 63);

    Date d = null;

    //
    // See if there is an earliest reported date for this license sig
    //
    long earliest = Long.MAX_VALUE;
    for (Preferences p : prefSet) {
      long l = p.getLong(sig, earliest);
      if (l < earliest) {
        earliest = l;
      }
    }

    if (earliest != Long.MAX_VALUE) {
      d = new Date(earliest);
    }

    return d;
  }

  public void setEarliestRun(String sig, Date date) {
    //
    // Java preferences only supports key lengths up to 80 characters.
    // We'll go with the first 64 to give us room to grow.
    //
    sig = sig.substring(0, 63);
    for (Preferences p : prefSet) {
      try {
        p.putLong(sig, date.getTime());
        p.flush();
      } catch (Exception e) {
      }
    }

  }
}
