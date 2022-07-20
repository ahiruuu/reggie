package com.ahiru.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ahiru.reggie.entity.AddressBook;
import com.ahiru.reggie.mapper.AddressBookMapper;
import com.ahiru.reggie.service.AddressBookService;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

}
