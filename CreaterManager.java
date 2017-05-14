/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.iv.api.Creater;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Lookup;

/**
 *
 * @author Junyi
 */
public class CreaterManager {

    private static Lookup.Result<Creater> creaters = Lookup.getDefault().lookupResult(Creater.class);

    public static List<Creater> getAllCreaters() {
        List<Creater> all = new ArrayList<Creater>();
        all.addAll(creaters.allInstances());
        return all;
    }

    public static List<Creater> getCreaters(int type) {
        List<Creater> all = new ArrayList<Creater>();

        for (Creater creater : creaters.allInstances()) {
            if (creater.getType() == type) {
                all.add(creater);
            }
        }
        return all;
    }
}
