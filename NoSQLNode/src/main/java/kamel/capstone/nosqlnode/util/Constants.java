package kamel.capstone.nosqlnode.util;

import java.io.File;

public class Constants {
    public static final String ROOT_DIRECTORY = "/var/nosql"; // "C:\\Users\\kamel\\OneDrive\\Desktop\\Cluster";
    public static final String COLLECTIONS_DIRECTORY = ROOT_DIRECTORY + "/data";
    public static final String META_DIRECTORY = ROOT_DIRECTORY + "/meta";
    public static final String ID_FILE_NAME = "id";
    public static final String ID_ATTRIBUTE_NAME = "_id";
    public static final String AFFINITY_ATTRIBUTE_NAME = "_affinity";
    public static final String AFFINITY_FILE_NAME = "affinity";
    public static final String PRIVATE_KEY = "atypon_training";
    public static final String BROADCAST_ENDPOINT = "/nosql/data-broadcast";
    public static final String COMMAND_ENDPOINT = "/execute";

    public static String getIdFilePath(String collectionName) {
        return getCollectionMetaDirectory(collectionName) + "/" + ID_FILE_NAME;
    }

    public static String getAffinityFilePath() {
        return META_DIRECTORY + "/" + AFFINITY_FILE_NAME;
    }

    public static String getCollectionDirectory(String collectionName) {
        return COLLECTIONS_DIRECTORY + "/" + collectionName;
    }

    public static String getCollectionDataDirectory(String collectionName) {
        return COLLECTIONS_DIRECTORY + "/" + collectionName + "/data";
    }

    public static String getCollectionMetaDirectory(String collectionName) {
        return COLLECTIONS_DIRECTORY + "/" + collectionName + "/meta";
    }

    public static String getCollectionIndexesDirectory(String collectionName) {
        return COLLECTIONS_DIRECTORY + "/" + collectionName + "/meta/indexes";
    }

    public static String getCollectionIndex(String collectionName, String column) {
        return COLLECTIONS_DIRECTORY + "/" + collectionName + "/meta/indexes/" + column;
    }
}
