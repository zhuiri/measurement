/**
 *
 */
package com.platformda.iv.admin;

import com.platformda.iv.api.Creater;
import com.platformda.iv.api.Instrument;
import com.platformda.iv.api.Profile;
import com.platformda.iv.api.ProfileEditor;

/**
 * Intrument by config
 *
 */
public class EntityInstrument {

    private Creater creater;
    private Profile profile;
    private ProfileEditor settingsEditor;
    private ProfileEditor optionsEditor;
    private Instrument instrument;
    private String instrumentName;
    private String driverName;
    public boolean enabled = true;

    public EntityInstrument(Creater creater, String instrumentName, String driverName) {
        this.creater = creater;
        this.instrumentName = instrumentName;
        this.driverName = driverName;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public ProfileEditor getSettingsEditor() {
        if (settingsEditor == null) {
            settingsEditor = creater.createSettingsEditor();
        }
        return settingsEditor;
    }

    public ProfileEditor getOptionsEditor() {
        if (optionsEditor == null) {
            optionsEditor = creater.createOptionsEditor();
        }
        return optionsEditor;
    }

    public Profile getProfile() {
        if (profile == null) {
            profile = creater.createProfile();
        }
        return profile;
    }

    public Instrument getInstrument() {
        if (instrument == null) {
            instrument = creater.createInstrument();
        }
        return instrument;
    }

    public String getDriverName() {
        return driverName;
    }

    public int getType() {
        return creater.getType();
    }

    public Creater getCreater() {
        return creater;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return getInstrumentName();
    }
}
