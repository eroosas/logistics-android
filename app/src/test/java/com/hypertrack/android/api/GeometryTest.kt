package com.hypertrack.android.api

import com.hypertrack.android.utils.Injector
import org.junit.Assert.*
import org.junit.Test

class GeometryTest {
    @Test
    fun itShouldDeserializePolygonsCorrectly() {
        val polygonString = """{"type": "Polygon", "coordinates": [[0.0, 0.0], [0.0, 0.1], [0.1, 0.0], [0.0, 0.0]]}"""
        val gson = Injector.getGson()
        val geometry = gson.fromJson(polygonString, Geometry::class.java)
        assertNotNull(geometry)
        assertTrue(geometry is Polygon)

    }

    @Test
    fun itShouldDeserializeCircularGeofenceCorrectly() {

        val geofenceString = """{
            "geofence_id": "00001111-4047-4b28-a6ec-f934e870c425",
            "device_id": "F3DF6D4F-6A06-4883-8201-D767EA408030",
            "geofence_metadata": { "station": "A" },
            "radius": 50,
            "geometry": {
                "type":"Point",
                "coordinates": [122.395223, 37.794763]
            }
        }"""
        val gson = Injector.getGson()
        val geofence = gson.fromJson(geofenceString, Geofence::class.java)
        assertNotNull(geofence)
        assertEquals("Point", geofence.type)

    }

    @Test
    fun itShouldDeserializePolygonGeofenceCorrectly() {

        val geofenceString = """{
            "geofence_id": "00001111-4047-4b28-a6ec-f934e870c425",
            "device_id": "F3DF6D4F-6A06-4883-8201-D767EA408030",
            "geofence_metadata": { "station": "B" },
            "geometry": {
                "type": "Polygon",
                "coordinates": [
                    [-122.395237, 37.7947693], 
                    [-122.402321, 37.794374],
                    [-122.401371, 37.790205], 
                    [-122.389450, 37.791271], 
                    [-122.395237, 37.7947693]
                ]
            }
        }"""
        val gson = Injector.getGson()
        val geofence = gson.fromJson(geofenceString, Geofence::class.java)
        assertNotNull(geofence)
        assertEquals("Polygon", geofence.type)
    }
}