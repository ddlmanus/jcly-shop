package com.zbkj.common.response;

public   class InventoryInfo {
        private String skuId;
        private String itemId;
        private Integer qty;
        private String name;
        private String modified;
        
        // Getter和Setter方法
        public String getSkuId() {
            return skuId;
        }
        
        public void setSkuId(String skuId) {
            this.skuId = skuId;
        }
        
        public String getItemId() {
            return itemId;
        }
        
        public void setItemId(String itemId) {
            this.itemId = itemId;
        }
        
        public Integer getQty() {
            return qty;
        }
        
        public void setQty(Integer qty) {
            this.qty = qty;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getModified() {
            return modified;
        }
        
        public void setModified(String modified) {
            this.modified = modified;
        }
    }