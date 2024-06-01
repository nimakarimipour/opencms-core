/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.util;

import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RUntainted;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RPolyTainted;

/**
 * Utility methods for processing geo coordinates.
 */
public final class CmsGeoUtil {

    /**
     * Parses coordinates, either from a lat,lon pair or from the JSON generated by the location picker.
     * @param coordinates the coordinates string
     * @return the parsed coordinates
     */
    public static String parseCoordinates(String coordinates) {

        if (coordinates == null) {
            return null;
        }
        String locationPickerCoordinates = CmsGeoUtil.parseLocationPickerCoordinates(coordinates);
        if (locationPickerCoordinates != null) {
            return locationPickerCoordinates;
        } else if (CmsGeoUtil.validateCoordinates(coordinates)) {
            return coordinates;
        }
        return null;
    }

    /**
     * Parses a location picker JSON value and returns the coordinates contained therein.
     * @param jsonValue the JSON value as string
     * @return the coordinates string or null if there are no valid location picker coordinates
     */
    public static String parseLocationPickerCoordinates(String jsonValue) {

        try {
            JSONObject json = new JSONObject(jsonValue);
            if (!CmsGeoUtil.validateLocationPickerCoordinates(json)) {
                return null;
            }
            return json.getString("lat") + "," + json.getString("lng");
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Validates a coordinates string and returns the validation result.
     * @param coordinates the coordinates string to validate
     * @return whether the coordinates are valid (true) or invalid (false)
     */
    public static boolean validateCoordinates(String coordinates) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(coordinates)) {
            return false;
        }
        if (!coordinates.contains(",")) {
            return false;
        }
        String[] tokens = coordinates.split(",");
        String latitude = tokens[0];
        String longitude = tokens[1];
        if (CmsGeoUtil.validateLatitude(latitude) && CmsGeoUtil.validateLongitude(longitude)) {
            return true;
        }
        return false;
    }

    /**
     * Validates a latitude string.
     * @param latitude the latitude string to validate
     * @return whether the string is a valid latitude value (true) or not (false)
     */
    public static boolean validateLatitude(String latitude) {

        try {
            double value = Double.parseDouble(latitude);
            return (value <= 90) && (value >= -90);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates the coordinates contained in a given location picker JSON value.
     * @param jsonObject the JSON value
     * @return whether JSON contains valid coordinates (true) or not (false)
     */
    public static boolean validateLocationPickerCoordinates(JSONObject jsonObject) {

        try {
            String latitude = jsonObject.getString("lat");
            String longitude = jsonObject.getString("lng");
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(latitude) || CmsStringUtil.isEmptyOrWhitespaceOnly(longitude)) {
                return false;
            }
            return validateLatitude(latitude) && CmsGeoUtil.validateLongitude(longitude);
        } catch (JSONException e) {
            return false;
        }
    }

    /**
     * Validates a longitude string.
     * @param longitude the longitude string to validate
     * @return whether the string is a valid longitude value (true) or not (false)
     */
    public static boolean validateLongitude(String longitude) {

        try {
            double value = Double.parseDouble(longitude);
            return (value <= 180) && (value >= -180);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates a radius string.
     * @param radius the radius
     * @return whether a valid radius string or not
     */
    public static boolean validateRadius(String radius) {

        try {
            double value = Double.parseDouble(radius);
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates a units string.
     * @param units the units string
     * @return whether a valid units string or not
     */
    public static boolean validateUnits(String units) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(units)) {
            return false;
        }
        return units.equals("km") || units.equals("mi");
    }
}
