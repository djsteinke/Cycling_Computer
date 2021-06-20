package rnfive.djs.cyclingcomputer.define.listeners;

import rnfive.djs.cyclingcomputer.define.enums.ConfirmResult;
import rnfive.djs.cyclingcomputer.define.enums.ConfirmType;

public interface ConfirmListener {
    void onConfirm(ConfirmType confirmType, ConfirmResult confirmResult, String[] strings);
}
