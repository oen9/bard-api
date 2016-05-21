package filters

import javax.inject.Inject

import play.api.http.HttpFilters
import play.filters.cors.CORSFilter

/**
  * Created by oen on 21.05.16.
  */
class Cors @Inject() (corsFilter: CORSFilter) extends HttpFilters {
  def filters = Seq(corsFilter)
}
