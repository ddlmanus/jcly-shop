-- 为商品分类表添加聚水潭分类ID字段
ALTER TABLE eb_product_category ADD COLUMN jst_category_id VARCHAR(50) COMMENT '聚水潭分类ID';

-- 为聚水潭分类ID字段添加索引以提高查询性能
CREATE INDEX idx_eb_product_category_jst_category_id ON eb_product_category(jst_category_id);