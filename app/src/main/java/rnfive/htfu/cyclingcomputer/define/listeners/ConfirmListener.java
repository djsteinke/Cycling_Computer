package rnfive.htfu.cyclingcomputer.define.listeners;

import rnfive.htfu.cyclingcomputer.define.enums.ConfirmResult;
import rnfive.htfu.cyclingcomputer.define.enums.ConfirmType;

public interface ConfirmListener {
    void onConfirm(ConfirmType confirmType, ConfirmResult confirmResult, String[] strings);
}
