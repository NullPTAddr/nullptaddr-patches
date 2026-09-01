package app.morphe.extension.dudu.patches.info


import app.morphe.extension.shared.requests.Requester
import app.morphe.extension.shared.requests.Route
import org.json.JSONObject

class WeatherInfo {
    companion object {
        const val key = "544142f25f5746daa2c74907230112"
        const val BASE_URL = "https://api.weatherapi.com/v1"

        @JvmStatic
        fun getWeatherInfo(coordinates: String): JSONObject {
            try {
                val route = Route(
                    Route.Method.GET,
                    "/forecast.json" + "?q=${coordinates}&days=1&key=$key"
                ).compile()
                val connection = Requester.getConnectionFromCompiledRoute(BASE_URL, route)
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000
                if (connection.responseCode == 200) {
                    val jsonObject = Requester.parseJSONObjectAndDisconnect(connection)
                    if (jsonObject.length() == 0) {
                        return JSONObject()
                    }
                    val resultObject = convertData(jsonObject) ?: JSONObject()

                    return resultObject
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return JSONObject()
        }

        fun convertData(inputObject: JSONObject): JSONObject? {
            val current = inputObject.optJSONObject("current") ?: return null
            val forecast = inputObject.optJSONObject("forecast") ?: return null
            val forecastday = forecast.optJSONArray("forecastday")?.optJSONObject(0) ?: return null
            val hours = forecastday.optJSONArray("hour") ?: return null

            if (hours.length() == 0) return null


            val localtime_epoch = current.opt("localtime_epoch")
            val localtime = current.optString("localtime").split(" ")[0]
            val currentTemp = current.optDouble("temp_c")
            val currentWind = current.optDouble("wind_kph")
            val conditionText = current.optJSONObject("condition")?.optString("text") ?: return null

            val result = JSONObject(
                """
                    {
                      "code": 0,
                      "data": {
                        "adcode": "530302",
                        "hws": []
                      },
                      "ntime": $localtime_epoch,
                      "dtime": $localtime,
                      "htime": 1766023360548,
                      "nt": "12.8",
                      "nw": "多云",
                      "dmin": "4",
                      "dmax": "15"
                    }
                """.trimIndent()
            )


            """
                      {
                          "code" : 0,
                          "data" : {
                            "adcode" : "530302",
                            "hws" : [ 
                                {
                                  "h" : 11,
                                  "s" : 1766026800,
                                  "t" : "9",
                                  "w" : "多云",
                                  "wd" : "西南风",
                                  "ws" : "1-3",
                                  "p" : "0.0",
                                  "pp" : "7"
                                }
                            ]
                          }
                          "ntime" : 1766034621718,
                          "dtime" : "2025-12-18",
                          "htime" : 1766023360548,
                          "nt" : "12.8",
                          "nw" : "多云",
                          "dmin" : "4",
                          "dmax" : "15"
                      }
            """.trimIndent()

            for (index in 0 until  hours.length()) {
                val hour = hours.optJSONObject(index)
                val newHour = JSONObject()
                newHour.put("h", 12) // hour
                newHour.put("s", 1766030400) // timestamp
                newHour.put("t", "12") // temp
                newHour.put("w", "多云") // condition text
                newHour.put("wd", "西南风") // wind direction
                newHour.put("ws", "1-3") //
                newHour.put("p", "0.0") //
                newHour.put("pp", "5") //
            }



            return result
        }
    }
}