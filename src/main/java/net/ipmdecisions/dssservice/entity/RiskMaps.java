/*
 * Copyright (c) 2021 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of IPMDecisionsDSSService.
 * IPMDecisionsDSSService is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * IPMDecisionsDSSService is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with IPMDecisionsDSSService.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package net.ipmdecisions.dssservice.entity;

import java.util.List;

/**
 * Data object structuring the list of risk maps available in the platform.
 * 
 * @copyright 2024 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class RiskMaps {
    
    private List<RiskMapProvider> risk_map_providers;
    
    /**
     * A risk map provider is an organization that has made one or more risk
     * maps available in the platform
     */
    public static class RiskMapProvider {
        private String id, name, country, address, postal_code, city, email, url;
        private RiskMap[] risk_maps;
        
        /**
         * A risk map is a WMS service that conforms to the VIPS risk maps
         * standard: https://gitlab.nibio.no/VIPS/documentation/-/blob/master/grid_models.md
         */
        public static class RiskMap {
            private String id, title, wms_url;
            private Boolean platform_validated;

            /**
             * @return the id
             */
            public String getId() {
                return id;
            }

            /**
             * @param id the id to set
             */
            public void setId(String id) {
                this.id = id;
            }

            /**
             * @return the title
             */
            public String getTitle() {
                return title;
            }

            /**
             * @param title the title to set
             */
            public void setTitle(String title) {
                this.title = title;
            }

            /**
             * @return the wms_url
             */
            public String getWms_url() {
                return wms_url;
            }

            /**
             * @param wms_url the wms_url to set
             */
            public void setWms_url(String wms_url) {
                this.wms_url = wms_url;
            }

            /**
             * @return the platform_validated
             */
            public Boolean getPlatform_validated() {
                return platform_validated;
            }

            /**
             * @param platform_validated the platform_validated to set
             */
            public void setPlatform_validated(Boolean platform_validated) {
                this.platform_validated = platform_validated;
            }
        }

        /**
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the country
         */
        public String getCountry() {
            return country;
        }

        /**
         * @param country the country to set
         */
        public void setCountry(String country) {
            this.country = country;
        }

        /**
         * @return the address
         */
        public String getAddress() {
            return address;
        }

        /**
         * @param address the address to set
         */
        public void setAddress(String address) {
            this.address = address;
        }

        /**
         * @return the postal_code
         */
        public String getPostal_code() {
            return postal_code;
        }

        /**
         * @param postal_code the postal_code to set
         */
        public void setPostal_code(String postal_code) {
            this.postal_code = postal_code;
        }

        /**
         * @return the city
         */
        public String getCity() {
            return city;
        }

        /**
         * @param city the city to set
         */
        public void setCity(String city) {
            this.city = city;
        }

        /**
         * @return the email
         */
        public String getEmail() {
            return email;
        }

        /**
         * @param email the email to set
         */
        public void setEmail(String email) {
            this.email = email;
        }

        /**
         * @return the url
         */
        public String getUrl() {
            return url;
        }

        /**
         * @param url the url to set
         */
        public void setUrl(String url) {
            this.url = url;
        }

        /**
         * @return the risk_maps
         */
        public RiskMap[] getRisk_maps() {
            return risk_maps;
        }

        /**
         * @param risk_maps the risk_maps to set
         */
        public void setRisk_maps(RiskMap[] risk_maps) {
            this.risk_maps = risk_maps;
        }
    }

    /**
     * @return the risk_map_providers
     */
    public List<RiskMapProvider> getRisk_map_providers() {
        return risk_map_providers;
    }

    /**
     * @param risk_map_providers the risk_map_providers to set
     */
    public void setRisk_map_providers(List<RiskMapProvider> risk_map_providers) {
        this.risk_map_providers = risk_map_providers;
    }
}
