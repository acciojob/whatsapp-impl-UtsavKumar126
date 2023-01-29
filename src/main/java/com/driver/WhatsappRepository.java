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
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }
    Set<User>users=new HashSet<>();
    Set<Message>messages=new HashSet<>();

    public String createUser(String name, String mobile) throws Exception {
        User user=new User(name,mobile);
        //If the mobile number exists in database, throw "User already exists" exception
        if(users.contains(user)){
            throw new Exception("User already exists");
        }
        //Otherwise, create the user and return "SUCCESS"
        users.add(user);

        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {
        Group group;
        // The list contains at least 2 users where the first user is the admin. A group has exactly one admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
        if(users.size()==2){
            group=new Group(users.get(1).getName(),users.size());
            groupUserMap.put(group,users);
            adminMap.put(group,users.get(0));
            groupMessageMap.put(group,new ArrayList<>());
        }
        // If there are 2+ users, the name of group should be "Group count". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
        else{
            group=new Group("Group "+customGroupCount++,users.size());
            groupUserMap.put(group,users);
            adminMap.put(group,users.get(0));
            groupMessageMap.put(group,new ArrayList<>());
        }
        // Note that a personal chat is not considered a group and the count is not updated for personal chats.
        // If group is successfully created, return group.

        //For example: Consider userList1 = {Alex, Bob, Charlie}, userList2 = {Dan, Evan}, userList3 = {Felix, Graham, Hugh}.
        //If createGroup is called for these userLists in the same order, their group names would be "Group 1", "Evan", and "Group 2" respectively.
        return group;
    }

    public int createMessage(String content) {
        // The 'i^th' created message has message id 'i'.
        Message message=new Message(messageId++,content);
        messages.add(message);
        // Return the message id.
        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        //Throw "Group does not exist" if the mentioned group does not exist
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        if(!groupUserMap.get(group).contains(sender)){
            throw new Exception("You are not allowed to send message");
        }
        //If the message is sent successfully, return the final number of messages in that group.
        List<Message>curr=groupMessageMap.get(group);
        curr.add(message);
        groupMessageMap.put(group,curr);
        senderMap.put(message,sender);
        return curr.size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        //Throw "Group does not exist" if the mentioned group does not exist
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        if(!adminMap.get(group).equals(approver)){
            throw new Exception("Approver does not have rights");
        }
        //Throw "User is not a participant" if the user is not a part of the group
        if(!groupUserMap.get(group).contains(user)){
            throw new Exception("User is not a participant");
        }
        //Change the admin of the group to "user" and return "SUCCESS". Note that at one time there is only one admin and the admin rights are transferred from approver to user.

        adminMap.put(group,user);
        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception {
        //This is a bonus problem and does not contains any marks
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        if(!users.contains(user)){
            throw new Exception("User not found");
        }
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        for(Group g: adminMap.keySet()){
            if(adminMap.get(g).equals(user)){
                throw new Exception("Cannot remove admin");
            }
        }
        Group relevent = null;
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        users.remove(user);
        for(Group g: groupUserMap.keySet()){
            if(groupUserMap.get(g).contains(user)){
                relevent=g;
                groupUserMap.get(g).remove(user);
                break;
            }
        }
        for(Message message: senderMap.keySet()){
            if(senderMap.get(message).equals(user)){
                senderMap.remove(message);
            }
        }
        for(Message message: senderMap.keySet()){
            if(!messages.contains(message)){
                messages.remove(message);
            }
        }
        List<Message>relevent2=groupMessageMap.get(relevent);
        for(Message m: relevent2){
            if(!senderMap.containsKey(m)){
                relevent2.remove(m);
            }
        }
        groupMessageMap.put(relevent,relevent2);
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)

        int num=groupUserMap.get(relevent).size()+groupMessageMap.get(relevent).size()+senderMap.size();

        return num;
    }

    public String findMessage(Date start, Date end, int k) throws Exception {
        //This is a bonus problem and does not contains any marks
        List<Message>messageList=new ArrayList<>();
        String messageContent="";
        for(Message message:messages){
            if(message.getTimestamp().after(start)&&message.getTimestamp().before(end)){
                messageList.add(message);
            }
        }
        // Find the Kth latest message between start and end (excluding start and end)
        if(messageList.size()<k){
            throw new Exception("K is greater than the number of messages");
        }
        // If the number of messages between given time is less than K, throw "K is greater than the number of messages" exception

        int count=1;

        for(int i=0;i<messageList.size();i++){
            count++;

            if(count ==k){
                messageContent=messageList.get(i).getContent();
                break;
            }
        }
        return messageContent;
    }
}