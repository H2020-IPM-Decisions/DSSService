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


package net.ipmdecisions.dssservice.services;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.jboss.resteasy.plugins.interceptors.CorsFilter;

/**
 * @copyright 2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@ApplicationPath("")
public class JAXActivator extends Application{

    @Override
    public Set<Class<?>> getClasses() {      
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClassesManually(resources);
        return resources;
    }
    
    @Override
    public Set<Object> getSingletons(){
        CorsFilter corsFilter = new CorsFilter();
        corsFilter.getAllowedOrigins().add("*");
        corsFilter.setAllowedMethods("OPTIONS, GET, POST, DELETE, PUT, PATCH");
        Set<Object> singletons = new HashSet<>();
        singletons.add(corsFilter);
        return singletons;
    }

    /* This method was named without the "Manually", and autopopulated by NetBeans, 
     * renamed it to keep in control
     */ 
    private void addRestResourceClassesManually(Set<Class<?>> resources) {
        resources.add(net.ipmdecisions.dssservice.services.AdminService.class);
        resources.add(net.ipmdecisions.dssservice.services.DSSService.class);
        resources.add(net.ipmdecisions.dssservice.services.MetaDataService.class);
        resources.add(net.ipmdecisions.dssservice.services.RiskMapService.class);
    }
}
