package com.studo.campusqr.endpoints

import com.moshbit.katerbase.*
import com.studo.campusqr.common.*
import com.studo.campusqr.database.BackendAccess
import com.studo.campusqr.database.BackendLocation
import com.studo.campusqr.database.CheckIn
import com.studo.campusqr.database.DateRange
import com.studo.campusqr.extensions.*
import com.studo.campusqr.utils.AuthenticatedApplicationCall
import io.ktor.features.*
import java.util.*


suspend fun getALLCheckINs(language: String): List<CheckIns> {
  return runOnDb { getCollection<CheckIn>().find().toList() }.map { it.toClientClass(language) }
}
suspend fun getCheckINsByLocation(language: String, locationId: String): List<CheckIns> {
  return runOnDb { getCollection<CheckIn>().find(CheckIn::locationId equal locationId).toList() }.map { it.toClientClass(language) }
}

suspend fun AuthenticatedApplicationCall.listAllCheckINs() {
  if (!sessionToken.isAuthenticated) {
    respondForbidden()
    return
  }

  val locationId = parameters["locationId"]

  if(locationId.isNullOrEmpty()) {
    respondObject(getALLCheckINs(language))
  }
  else{
    respondObject(getCheckINsByLocation(language,locationId.toString()))
  }

}