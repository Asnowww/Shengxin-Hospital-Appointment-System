package org.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.mapper.AppointmentTypeMapper;
import org.example.backend.pojo.AppointmentType;
import org.example.backend.service.AppointmentTypeService;
import org.springframework.stereotype.Service;


@Service
public class AppointmentTypeServiceImpl extends ServiceImpl<AppointmentTypeMapper, AppointmentType>
        implements AppointmentTypeService {
}
