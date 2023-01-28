package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) throws Exception {
        if(userMobile.contains(mobile)){
            throw new Exception("User already exists");
        }
        userMobile.add(mobile);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {
        Group group = null;
        if(users.size()==2){
            group  = new Group(users.get(1).getName(),users.size());
        }else{
            customGroupCount++;
            group  = new Group("Group " + customGroupCount,users.size());
        }
        adminMap.put(group, users.get(0));
        return group;
    }

    public int createMessage(String content) {
        messageId++;
        Message message = new Message(messageId, content);
        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        //Throw "Group does not exist" if the mentioned group does not exist
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        if(groupUserMap.get(group).contains(sender)){
            throw new Exception("You are not allowed to send message");
        }
        //If the message is sent successfully, return the final number of messages in that group.
        List<Message> messageList = groupMessageMap.get(group) == null ? new ArrayList<>() : groupMessageMap.get(group) ;
        messageList.add(message);
        senderMap.put(message,sender);
        groupMessageMap.put(group, messageList);
        return messageList.size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        //Throw "Group does not exist" if the mentioned group does not exist
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        //Throw "Approver does not have rights" if the approver is not the current admin of the group

        if(!adminMap.get(group).getMobile().equals(approver.getMobile())){
           throw new Exception("Approver does not have rights");
        }
        //Throw "User is not a participant" if the user is not a part of the group
        if(groupUserMap.get(group).contains(user)){
            throw new Exception("User is not a participant");
        }
        //Change the admin of the group to "user" and return "SUCCES". Note that at one time there is only one admin and the admin rights are transferred from approver to user.

        adminMap.put(group,user);

        return "SUCCESS";
    }

    public int removeUser(User user) {
        //This is a bonus problem and does not contains any marks
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)
        return 0;

    }

    public String findMessage(Date start, Date end, int k) {
        //This is a bonus problem and does not contains any marks
        // Find the Kth latest message between start and end (excluding start and end)
        // If the number of messages between given time is less than K, throw "K is greater than the number of messages" exception
        ArrayList<Message> messageList = new ArrayList<>(senderMap.keySet());
        messageList.sort(Comparator.comparing(Message::getTimestamp));

        int counter = 0;
        int kthLatestIndex = -1;
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).getTimestamp().before(start) ||
                    messageList.get(i).getTimestamp().after(end)) continue;
            counter++;
            if (counter == k) {
                kthLatestIndex = i;
                break;
            }
        }

        return messageList.get(kthLatestIndex).getContent();
    }
}
