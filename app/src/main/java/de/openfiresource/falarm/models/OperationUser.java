package de.openfiresource.falarm.models;

import android.content.Context;

import com.orhanobut.logger.Logger;
import com.orm.SugarRecord;

import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.openfiresource.falarm.utils.EncryptionUtils;

/**
 * Created by Kevin on 25.10.2016.
 */

public class OperationUser extends SugarRecord {
    OperationMessage operationMessage;
    boolean come;
    String name;

    public OperationUser() {
    }

    public OperationUser(OperationMessage operationMessage, boolean come, String name) {
        this.operationMessage = operationMessage;
        this.come = come;
        this.name = name;
    }

    public OperationMessage getOperationMessage() {
        return operationMessage;
    }

    public void setOperationMessage(OperationMessage operationMessage) {
        this.operationMessage = operationMessage;
    }

    public boolean isCome() {
        return come;
    }

    public void setCome(boolean come) {
        this.come = come;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static OperationUser fromFCM(Context context, Map<String, String> extras) {
        OperationUser incoming = new OperationUser();
        Set<String> keys = extras.keySet();
        try {
            for (String string : keys) {
                String value = URLDecoder.decode(extras.get(string), EncryptionUtils.CHARACTER_ENCODING);
                switch (string) {
                    case "name":
                        incoming.setName(value);
                        break;
                    case "come":
                        if(value.equals("true"))
                            incoming.setCome(true);
                        else
                            incoming.setCome(false);
                        break;
                    case "operation":
                        List<OperationMessage> operationMessageList
                                = OperationMessage.find(OperationMessage.class, "key = ?", value);
                        if(operationMessageList.size() > 0)
                            incoming.setOperationMessage(operationMessageList.get(0));
                        break;
                }
            }
        } catch (Exception exception) {
            Logger.e(exception, "Error parsing incoming Operation");
            return null;
        }

        return incoming;
    }
}
