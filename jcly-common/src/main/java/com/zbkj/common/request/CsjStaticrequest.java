package com.zbkj.common.request;

import com.zbkj.common.model.csj.CsjAreaStatic;
import com.zbkj.common.model.csj.CsjStatic;
import lombok.Data;

import java.util.List;

@Data
public class CsjStaticrequest extends CsjStatic {
    private List<CsjAreaStatic> csjAreaStatics;
}
