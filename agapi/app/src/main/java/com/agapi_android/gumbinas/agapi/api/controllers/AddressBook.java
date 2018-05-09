package com.agapi_android.gumbinas.agapi.api.controllers;

import android.support.annotation.NonNull;

import com.agapi_android.gumbinas.agapi.api.handlers.AgapiDBHandler;
import com.agapi_android.gumbinas.agapi.api.models.Contact;

import java.util.ArrayList;
import java.util.List;

public class AddressBook {

    private List<Contact> _contacts;
    private AgapiDBHandler _adbHandler;

    AddressBook(AgapiDBHandler adbHandler) {
        _adbHandler = adbHandler;
        load();
    }

    public boolean isEmpty() {
        return _contacts.isEmpty();
    }

    public List<Contact> getContacts() {
        List<Contact> contactsCopy = new ArrayList<>();
        for (Contact contact : _contacts) {
            Contact contactCopy = new Contact();
            contactCopy.id = contact.id;
            contactCopy.name = contact.name;
            contactCopy.phone = contact.phone;
            contactCopy.email = contact.email;
            contactCopy.personalMessage = contact.personalMessage;
            contactCopy.inQueue = contact.inQueue;
            contactsCopy.add(contactCopy);
        }
        return contactsCopy;
    }

    public void load() {
        _contacts = _adbHandler.getAllContacts();
    }

    public void saveContact(@NonNull Contact contact) {
        boolean updated = false;
        for (Contact oldContact : _contacts) {
            if (contact.id == oldContact.id) {
                _adbHandler.updateContact(contact);
                oldContact.name = contact.name;
                oldContact.phone = contact.phone;
                oldContact.email = contact.email;
                oldContact.personalMessage = contact.personalMessage;
                oldContact.inQueue = contact.inQueue;
                updated = true;
                break;
            }
        }
        if (!updated) {
            contact.id = _adbHandler.addContact(contact);
            _contacts.add(contact);
        }
    }

    public void deleteContact(long id) {
        _adbHandler.removeContact(id);
        for (Contact contact : _contacts) {
            if (contact.id == id) {
                _contacts.remove(contact);
                break;
            }
        }
    }

    public void clear() {
        for (Contact contact : _contacts)
            _adbHandler.removeContact(contact.id);
        _contacts.clear();
    }

}
