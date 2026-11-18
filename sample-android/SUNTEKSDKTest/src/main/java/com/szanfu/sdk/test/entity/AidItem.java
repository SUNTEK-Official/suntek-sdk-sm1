package com.szanfu.sdk.test.entity;

import java.util.List;


public class AidItem {


    private List<AidListDTO> aidList;

    public List<AidListDTO> getAidList() {
        return aidList;
    }

    public void setAidList(List<AidListDTO> aidList) {
        this.aidList = aidList;
    }

    public static class AidListDTO {
        private String aidName;
        private String aidValue;

        public String getAidName() {
            return aidName;
        }

        public void setAidName(String aidName) {
            this.aidName = aidName;
        }

        public String getAidValue() {
            return aidValue;
        }

        public void setAidValue(String aidValue) {
            this.aidValue = aidValue;
        }
    }
}
