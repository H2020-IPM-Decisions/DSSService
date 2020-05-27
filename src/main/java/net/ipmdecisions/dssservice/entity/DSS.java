/*
 * Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
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
import java.util.List;

/**
 * This data object is the Decision Support System that contains one or more
 * DSS Models. So if we are describing a system that has only one model,
 * we still will use this "umbrella" object.
 * 
 * @copyright 2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class DSS {
    private List<DSSModel> models;
    private String id, version, name,url;
    private List<String> languages;
    private Organization organization;
    
    /**
     * A data class for identifying the Organization behind/responsible for the DSS
     */
    static class Organization {
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

    /**
     * @return The models in this particular DSS. 
     */
    public List<DSSModel> getModels() {
        return models;
    }

    /**
     * @param models the models to set
     */
    public void setModels(List<DSSModel> models) {
        this.models = models;
    }

    /**
     * @return A unique ID for the DSS. Preferably a reverse domain. E.g. no.nibio.vips
     */
    @DocumentationExample("no.nibio.vips")
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
     * @return The version of the DSS. Important to distinguish the 
     * usage of the DSS over time, and the definitions of the models may also 
     * change.
     */
    @DocumentationExample("2.0")
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return  E.g. VIPS, Crop Protection Online
     */
    @DocumentationExample("VIPS")
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
     * @return A URL to the DSS. E.g. https://www.vips-landbruk.no/ or https://plantevaernonline.dlbr.dk/
     */
    @DocumentationExample("https://www.vips-landbruk.no/")
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
     * @return A list of languages that the DSS supports
     */
    @DocumentationExample(value="Norwegian",value2="English")
    public List<String> getLanguages() {
        return languages;
    }

    /**
     * @param languages the languages to set
     */
    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    /**
     * @return The Organization behind/responsible for the DSS. E.g. NIBIO, ADAS, SEGES
     */
    public Organization getOrganization() {
        return organization;
    }

    /**
     * @param organization the organization to set
     */
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
}
