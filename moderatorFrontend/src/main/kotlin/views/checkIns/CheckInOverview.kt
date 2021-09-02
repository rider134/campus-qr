package views.checkIns

import app.GlobalCss
import app.routeContext
import com.studo.campusqr.common.ActiveCheckIn
import com.studo.campusqr.common.ClientLocation
import react.*
import react.dom.div
import util.Strings
import util.apiBase
import util.get
import views.common.*
import webcore.MbSnackbarProps
import webcore.NetworkManager
import webcore.extensions.launch
import webcore.materialUI.*
import webcore.mbSnackbar

interface CheckInOverviewProps : RProps {
  var classes: CheckInOverviewClasses
}

interface CheckInOverviewState : RState {
  var activeGuestCheckIns: List<ActiveCheckIn>?
  var showAddGuestCheckInDialog: Boolean
  var loadingCheckInList: Boolean
  var snackbarText: String

  var locationFetchInProgress: Boolean
  var showProgress: Boolean
  var locationNameToLocationMap: Map<String, ClientLocation>

  var selectedLocation: ClientLocation?
  var selectedLocationTextFieldError: String
}

class CheckInOverview : RComponent<CheckInOverviewProps, CheckInOverviewState>() {

  override fun CheckInOverviewState.init() {
    activeGuestCheckIns = emptyList()
    showAddGuestCheckInDialog = false
    loadingCheckInList = false
    snackbarText = ""

    locationFetchInProgress = false
    showProgress = false
    locationNameToLocationMap = emptyMap()

    selectedLocation = null
    selectedLocationTextFieldError = ""
  }

  private fun fetchLocations() = launch {
    setState {
      locationFetchInProgress = true
    }
    val response = NetworkManager.get<Array<ClientLocation>>("$apiBase/location/list")
    setState {
      if (response != null) {
        locationNameToLocationMap = response.associateBy { it.name }
      }
      locationFetchInProgress = false
    }
  }

  private fun fetchActiveCheckIns() = launch {
    setState { loadingCheckInList }
    val response = NetworkManager.get<Array<ActiveCheckIn>>("$apiBase/checkIns",)
    setState {
      if (response != null) {
        activeGuestCheckIns = response.toList()
      } else {
        snackbarText = Strings.error_try_again.get()
      }
      loadingCheckInList = false
    }
  }

  private fun fetchActiveCheckInsByLocation(locationid: String) = launch {
    setState { loadingCheckInList }
    val response = NetworkManager.get<Array<ActiveCheckIn>>("$apiBase/checkIns", mapOf("locationid" to locationid))
    setState {
      if (response != null) {
        activeGuestCheckIns = response.toList()
      } else {
        snackbarText = Strings.error_try_again.get()
      }
      loadingCheckInList = false
    }
  }

  override fun componentDidMount() {
    fetchLocations()
    fetchActiveCheckIns()
  }


  private fun RBuilder.renderSnackbar() = mbSnackbar(
    MbSnackbarProps.Config(
      show = state.snackbarText.isNotEmpty(),
      message = state.snackbarText,
      onClose = {
        setState { snackbarText = "" }
      })
  )

  override fun RBuilder.render() {
    renderSnackbar()
    renderToolbarView(ToolbarViewProps.Config(
      title = Strings.guest_checkin.get(),
      buttons = listOf(
        ToolbarViewProps.ToolbarButton(
          text = Strings.guest_checkin_add_guest.get(),
          variant = "contained",
          onClick = {
            setState {
              showAddGuestCheckInDialog = true
            }
          }
        )
      )
    ))

    muiAutocomplete {
      attrs.value = state.selectedLocation?.name ?: ""
      attrs.onChange = { _, target: String?, _ ->
        setState {
          selectedLocationTextFieldError = ""
          selectedLocation = target?.let { locationNameToLocationMap[it] }
          //selectedLocation.id
          // User should re-select seat if needed
          //seatInputValue = null
          fetchActiveCheckInsByLocation(selectedLocation?.id!!)
        }
      }
      attrs.openOnFocus = true
      attrs.options = state.locationNameToLocationMap.keys.toTypedArray()
      attrs.getOptionLabel = { it }
      attrs.renderInput = { params: dynamic ->
        textField {
          attrs.error = state.selectedLocationTextFieldError.isNotEmpty()
          attrs.helperText = state.selectedLocationTextFieldError
          attrs.id = params.id
          attrs.InputProps = params.InputProps
          attrs.inputProps = params.inputProps
          attrs.disabled = params.disabled
          attrs.fullWidth = params.fullWidth
          attrs.fullWidth = true
          attrs.variant = "outlined"
          attrs.label = Strings.location_name.get()
        }
      }
    }

    renderLinearProgress(state.loadingCheckInList)

      when {
        state.activeGuestCheckIns?.isNotEmpty() == true -> mTable {
          mTableHead {
            mTableRow {
              mTableCell { +Strings.email_address.get() }
              mTableCell { +Strings.user_first_login_date.get() }
              mTableCell { }
            }
          }
          mTableBody {
            state.activeGuestCheckIns!!.forEach { activeCheckIn ->
              renderCheckInRow(
                CheckInRowProps.Config(activeCheckIn)
              )
            }
          }
        }
        state.activeGuestCheckIns == null && !state.loadingCheckInList -> networkErrorView()
        !state.loadingCheckInList -> genericErrorView(
          Strings.guest_checkin_not_yet_added_title.get(),
          Strings.guest_checkin_not_yet_added_subtitle.get()
        )
      }
    }
  }

  interface CheckInOverviewClasses

  private val style = { _: dynamic ->
  }

  private val styled = withStyles<CheckInOverviewProps, CheckInOverview>(style)

  fun RBuilder.renderCheckInOverview() = styled {
    // Set component attrs here
  }
