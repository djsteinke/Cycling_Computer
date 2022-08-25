package rnfive.htfu.cyclingcomputer.strava.runnable;

import java.io.File;

import lombok.Getter;
import lombok.Setter;
import rnfive.htfu.cyclingcomputer.define.Files;
import rn5.djs.stravalib.common.api.StravaConfig;
import rn5.djs.stravalib.common.model.StravaResponse;
import rn5.djs.stravalib.upload.api.UploadAPI;
import rn5.djs.stravalib.upload.model.FileType;
import rn5.djs.stravalib.upload.model.UploadActivityType;
import rn5.djs.stravalib.upload.model.UploadStatus;
import rnfive.htfu.cyclingcomputer.MainActivity;
import rnfive.htfu.cyclingcomputer.define.listeners.StravaUploadResponseListener;

@Getter
@Setter
public class Runnable_StravaUpload implements Runnable {

    private final static String TAG = Runnable_StravaUpload.class.getSimpleName();
    private final String file_name;
    private final String activity_name;
    private final String activity_desc;
    private boolean runActivity = false;
    private StravaUploadResponseListener listener;

    public Runnable_StravaUpload(String file_name, String activity_name, String activity_desc) {
        this.file_name = file_name;
        this.activity_name = activity_name;
        this.activity_desc = activity_desc;
    }

    public Runnable_StravaUpload withListener(StravaUploadResponseListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public void run() {
        String result = "";

        UploadActivityType actType = (runActivity?UploadActivityType.RUN:UploadActivityType.RIDE);
        File activityFile = new File(MainActivity.filePathApp, file_name);

        try {
            StravaConfig config = StravaConfig.withToken(MainActivity.token)
                    .debug()
                    .build();
            UploadAPI uploadAPI = new UploadAPI(config);
            StravaResponse<UploadStatus> uploadStatus = uploadAPI.uploadFile(activityFile)
                    .withDataType(FileType.FIT)
                    .withActivityType(actType)
                    .withDescription(activity_desc)
                    .withName(activity_name)
                    .isPrivate(MainActivity.bPrivate)
                    .hasTrainer(MainActivity.bTrainer)
                    .isCommute(MainActivity.bCommute)
                    .withExternalId(file_name)
                    .executeWithResponse();
            if (uploadStatus.getCode() == 201) {
                result = "Upload successful [" + uploadStatus.getCode() + "]\n";
            }
            result += uploadStatus.getResponse().getStatus();
        } catch (Exception e ) {
            result = e.getMessage();
            Files.logMesg("E",TAG, e.getMessage());
        }

        if (listener != null) {
            listener.onStravaResponse(result);
        }
        MainActivity.toastListener.onToast(result);
    }
}
