-- 为商品表添加聚水潭相关字段
ALTER TABLE eb_product ADD COLUMN jst_item_id VARCHAR(50) COMMENT '聚水潭商品ID';
ALTER TABLE eb_product ADD COLUMN jst_sku_id VARCHAR(50) COMMENT '聚水潭商品编码';

-- 为聚水潭字段添加索引以提高查询性能
CREATE INDEX idx_eb_product_jst_item_id ON eb_product(jst_item_id);
CREATE INDEX idx_eb_product_jst_sku_id ON eb_product(jst_sku_id);