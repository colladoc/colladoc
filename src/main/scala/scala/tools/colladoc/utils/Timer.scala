package scala.tools.colladoc.utils

/**
 * Created by IntelliJ IDEA.
 * User: rumi
 * Date: 24/02/11
 * Time: 22:04
 * To change this template use File | Settings | File Templates.
 */

object Timer {
  var start:Long = 0L
  var end:Long = 0L
  def go = {
    start = System.currentTimeMillis
  }
  def stop = {
    end = System.currentTimeMillis
    println(">   " + (end - start)/ 1000.0 + " s")
  }
}