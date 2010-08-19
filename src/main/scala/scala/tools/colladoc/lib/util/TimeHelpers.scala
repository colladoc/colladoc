/*
 * Copyright (c) 2010, Petr Hosek. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and
 *     the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *     and the following disclaimer in the documentation and/or other materials provided with the
 *     distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COLLABORATIVE SCALADOC PROJECT ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COLLABORATIVE SCALADOC
 * PROJECT OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package scala.tools.colladoc {
package lib {
package util {

import java.util.{Date, Calendar}

/**
 * Provides utility functions for working with date and time.
 * @author Petr Hosek
 */
trait TimeHelpers {

  /** Transforms a calendar to a date. */
  implicit def toDate(c: Calendar) = c.getTime

  /** Transforms a date to a time (in milliseconds). */
  implicit def toTime(d: Date) = d.getTime

  /** Transforms a calendar to extended calendar providing fluent interface. */
  implicit def toCalendar(c: Calendar) = new CalendarExtensions(c)

  /**
   * Extends Calendar class to provide more fluent interface.
   */
  class CalendarExtensions(c: Calendar) {
    def rollDay(a: Int) = { c.roll(Calendar.DAY_OF_MONTH, a); c }
    def rollMonth(a: Int) = { c.roll(Calendar.MONTH, a); c }
    def rollYear(a: Int) = { c.roll(Calendar.YEAR, a); c }
  }

  /**
   * Returns timestamp corresponding to time in milliseconds.
   * @return timestamp in milliseconds
   */
  def timestamp(time: Long) = new java.sql.Timestamp(time)

}

}
}
}