package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = EntityFolder.TABLE_NAME,
        foreignKeys = {
                @ForeignKey(childColumns = "account", entity = EntityAccount.class, parentColumns = "id", onDelete = CASCADE)
        },
        indices = {
                @Index(value = {"account", "name"}, unique = true),
                @Index(value = {"account"}),
                @Index(value = {"name"}),
                @Index(value = {"type"}),
                @Index(value = {"unified"})
        }
)

public class EntityFolder implements Serializable {
    static final String TABLE_NAME = "folder";

    @PrimaryKey(autoGenerate = true)
    public Long id;
    public Long account; // Outbox = null
    @NonNull
    public String name;
    @NonNull
    public String type;
    @NonNull
    public Integer level;
    @NonNull
    public Boolean synchronize;
    @NonNull
    public Boolean poll = false;
    @NonNull
    public Boolean download = true;
    @NonNull
    public Integer sync_days;
    @NonNull
    public Integer keep_days;
    @NonNull
    public Boolean initialize = true;
    public String display;
    @NonNull
    public Boolean hide = false;
    @NonNull
    public Boolean unified = false;
    @NonNull
    public Boolean notify = false;
    public String[] keywords;
    public Boolean tbd;
    public String state;
    public String sync_state;
    public String error;

    static final String INBOX = "Inbox";
    static final String OUTBOX = "Outbox";
    static final String ARCHIVE = "All";
    static final String DRAFTS = "Drafts";
    static final String TRASH = "Trash";
    static final String JUNK = "Junk";
    static final String SENT = "Sent";
    static final String SYSTEM = "System";
    static final String USER = "User";

    static final List<String> SYSTEM_FOLDER_ATTR = Arrays.asList(
            "All",
            "Drafts",
            "Trash",
            "Junk",
            "Sent",
            "Important",
            "Flagged"
    );
    static final List<String> SYSTEM_FOLDER_TYPE = Arrays.asList(
            ARCHIVE,
            DRAFTS,
            TRASH,
            JUNK,
            SENT,
            SYSTEM,
            SYSTEM
    ); // MUST match SYSTEM_FOLDER_ATTR

    static final List<String> FOLDER_SORT_ORDER = Arrays.asList(
            INBOX,
            OUTBOX,
            DRAFTS,
            SENT,
            ARCHIVE,
            TRASH,
            JUNK,
            SYSTEM,
            USER
    );

    static final int DEFAULT_INIT = 7; // days
    static final int DEFAULT_SYNC = 1; // days
    static final int DEFAULT_KEEP = 14; // days

    static final List<String> SYSTEM_FOLDER_SYNC = Arrays.asList(
            DRAFTS,
            SENT,
            ARCHIVE,
            TRASH
            // not spam
    );

    public EntityFolder() {
    }

    String getDisplayName(Context context) {
        return (display == null ? Helper.localizeFolderName(context, name) : display);
    }

    boolean isOutgoing() {
        return isOutgoing(this.type);
    }

    static boolean isOutgoing(String type) {
        return DRAFTS.equals(type) || OUTBOX.equals(type) || SENT.equals(type);
    }

    static int getLevel(Character separator, String name) {
        int level = 0;
        if (separator != null)
            for (int i = 0; i < name.length(); i++)
                if (name.charAt(i) == separator)
                    level++;
        return level;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityFolder) {
            EntityFolder other = (EntityFolder) obj;
            return (this.id.equals(other.id) &&
                    (this.account == null ? other.account == null : this.account.equals(other.account)) &&
                    this.name.equals(other.name) &&
                    this.type.equals(other.type) &&
                    this.level.equals(other.level) &&
                    this.synchronize.equals(other.synchronize) &&
                    this.poll.equals(other.poll) &&
                    this.sync_days.equals(other.sync_days) &&
                    this.keep_days.equals(other.keep_days) &&
                    (this.display == null ? other.display == null : this.display.equals(other.display)) &&
                    this.hide == other.hide &&
                    this.unified == other.unified &&
                    this.notify == other.notify &&
                    Helper.equal(this.keywords, other.keywords) &&
                    (this.tbd == null ? other.tbd == null : this.tbd.equals(other.tbd)) &&
                    (this.state == null ? other.state == null : this.state.equals(other.state)) &&
                    (this.sync_state == null ? other.sync_state == null : this.sync_state.equals(other.sync_state)) &&
                    (this.error == null ? other.error == null : this.error.equals(other.error)));
        } else
            return false;
    }

    @Override
    public String toString() {
        return name;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("type", type);
        json.put("level", level);
        json.put("synchronize", synchronize);
        json.put("poll", poll);
        json.put("sync_days", sync_days);
        json.put("keep_days", keep_days);
        json.put("display", display);
        json.put("hide", hide);
        json.put("unified", unified);
        json.put("notify", notify);
        return json;
    }

    public static EntityFolder fromJSON(JSONObject json) throws JSONException {
        EntityFolder folder = new EntityFolder();
        folder.name = json.getString("name");
        folder.type = json.getString("type");

        if (json.has("level"))
            folder.level = json.getInt("level");
        else
            folder.level = 0;

        folder.synchronize = json.getBoolean("synchronize");
        if (json.has("poll"))
            folder.poll = json.getBoolean("poll");
        else
            folder.poll = false;

        if (json.has("after"))
            folder.sync_days = json.getInt("after");
        else
            folder.sync_days = json.getInt("sync_days");

        if (json.has("keep_days"))
            folder.keep_days = json.getInt("keep_days");
        else
            folder.keep_days = folder.sync_days;

        if (json.has("display"))
            folder.display = json.getString("display");

        if (json.has("hide"))
            folder.hide = json.getBoolean("hide");

        folder.unified = json.getBoolean("unified");

        if (json.has("notify"))
            folder.notify = json.getBoolean("notify");

        return folder;
    }

    static void sort(List<EntityFolder> folders) {
        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        Collections.sort(folders, new Comparator<EntityFolder>() {
            @Override
            public int compare(EntityFolder f1, EntityFolder f2) {
                int s = Integer.compare(
                        EntityFolder.FOLDER_SORT_ORDER.indexOf(f1.type),
                        EntityFolder.FOLDER_SORT_ORDER.indexOf(f2.type));
                if (s != 0)
                    return s;
                int c = -f1.synchronize.compareTo(f2.synchronize);
                if (c != 0)
                    return c;
                return collator.compare(
                        f1.name == null ? "" : f1.name,
                        f2.name == null ? "" : f2.name);
            }
        });
    }
}
