/*
 * Copyright (c) 2024 NIBIO <http://www.nibio.no/>. 
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

import com.webcohesion.enunciate.metadata.DocumentationExample;

/**
 * Representing DSS provider organization
 * 
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class Organization {
    private String name, country, address, postal_code, city, email, url;

        /**
         * @return the name of the Organization. E.g. ADAS, NIBIO
         */
        @DocumentationExample("NIBIO")
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
         * @return the country of the Organization
         */
        @DocumentationExample("Norway")
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
        @DocumentationExample("Postboks 115")
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
        @DocumentationExample("1431")
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
        @DocumentationExample("Ås")
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
         * @return the email. Preferably the email to a person
         * or department responsible for the DSS
         */
        @DocumentationExample("acme@foobar.com")
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
}
