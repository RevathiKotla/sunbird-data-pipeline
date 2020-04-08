package org.sunbird.dp.domain

import java.util

class Event(eventMap: util.Map[String, AnyRef], partition: Integer) extends Events(eventMap, partition) {

  def markDuplicate(): Unit = {
    telemetry.addFieldIfAbsent("flags", new util.HashMap[String, Boolean])
    telemetry.add("flags.dd_processed", false)
    telemetry.add("flags.dd_duplicate_event", true)
  }

  def markSuccess(): Unit = {
    telemetry.addFieldIfAbsent("flags", new util.HashMap[String, Boolean])
    telemetry.add("flags.dd_processed", true)
    telemetry.add("type", "events")
  }

}
