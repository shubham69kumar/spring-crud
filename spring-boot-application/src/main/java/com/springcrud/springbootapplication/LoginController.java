package com.springcrud.springbootapplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.springcrud.springbootapplication.models.LoginData;
import com.springcrud.springbootapplication.models.Newuser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


@Controller
public class LoginController {
    private String token_key;

    @GetMapping("/")
    public String welcome(Model model) {

        model.addAttribute("logindata", new LoginData());

        return "index";
    }

    @PostMapping("/processlogin")
    public String login(@ModelAttribute LoginData logindata, Model model) throws IOException {
        URL url = new URL("https://qa2.sunbasedata.com/sunbase/portal/api/assignment_auth.jsp");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        String jsonInputString = new Gson().toJson(logindata);
        System.out.println(jsonInputString);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {

            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.substring(17, 57));
            token_key = response.substring(17, 57);
        } catch (IOException ioe) {
            int status = con.getResponseCode();
            if (status == 500) {
                System.out.println(status);
                model.addAttribute("logindata", new LoginData());
                model.addAttribute("Invalid", "Either the id of password is wrong try again ");
                return "index";
            }


        }
        System.out.println(logindata.getLogin_id());
        System.out.println(logindata.getPassword());

        model.addAttribute("jsondata", new ArrayList<>());
        return "main";
    }

    @GetMapping("/createuser")
    public String createUser(Model model) {
        model.addAttribute("token", token_key);
        model.addAttribute("newuser", new Newuser());
        return "regester";
    }

    @PostMapping("/createuser")
    public String create(@ModelAttribute Newuser newuser, Model model) throws IOException {
        URL url = new URL(" https://qa2.sunbasedata.com/sunbase/portal/api/assignment.jsp?" + "cmd=" + "create" );
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "Bearer " + token_key);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        if (newuser.getFirst_name() == ""){
            newuser.setFirst_name(null);
        }
        if (newuser.getLast_name() == ""){
            newuser.setLast_name(null);
        }
        String jsonInputString = new Gson().toJson(newuser);
        System.out.println(jsonInputString);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
            int status = con.getResponseCode();
            System.out.println(con.getResponseCode());
            if (status == 201) {
                System.out.println(status);
                model.addAttribute("message", "User added use list option to see the user");
                model.addAttribute("jsondata", new ArrayList<>());
                return "main";
            } else if (status == 400) {
                model.addAttribute("message", "Either the first name of the last name is null Try again");
                return "regester";
            }
            else {
                model.addAttribute("message", "Something went wrong try again");
                model.addAttribute("jsondata", new ArrayList<>());
                return "main";
            }


        }


    @GetMapping("/listuser")
    public String listUser(Model model) throws IOException{
        URL url = new URL("https://qa2.sunbasedata.com/sunbase/portal/api/assignment.jsp?cmd=get_customer_list");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + token_key);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {

            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            ObjectMapper objectMapper = new ObjectMapper();
            List<Newuser> userList;
            try {
                userList = objectMapper.readValue(response.toString(), new TypeReference<List<Newuser>>() {});
            } catch (IOException e) {
                e.printStackTrace();
                userList = new ArrayList<>(); // Empty list in case of error
            }

            model.addAttribute("jsondata", userList);
            return "main";
        }
        catch (IOException ioException){
            model.addAttribute("message", "Something went wrong try again");
            model.addAttribute("jsondata", new ArrayList<>());
            return "main";
        }
    }

    @RequestMapping("/deleteBook/{id}")
    public String deleteBook(@PathVariable("id")String id , RedirectAttributes redirectAttributes) throws IOException {
        URL url = new URL("https://qa2.sunbasedata.com/sunbase/portal/api/assignment.jsp?cmd=delete&uuid="+id);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "Bearer " + token_key);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        int status = con.getResponseCode();
        if (status == 200){
            redirectAttributes.addAttribute("message", "User is deleted");
        }
        else if (status == 500){
            redirectAttributes.addAttribute("message", "Something went wrong try again ");
        }
        else if (status == 400){
            redirectAttributes.addAttribute("message", "UUID is wrong try again ");
        }

        return "redirect:/listuser";
    }

    @RequestMapping("/edituser/{id}")
    public String edituser(@PathVariable("id")String id ,Model model) {

        model.addAttribute("uuid" , id);
        model.addAttribute("newuser", new Newuser());
        return "edituser";
    }

    @PostMapping("/updateuser/{id}")
    public String updateuser(@PathVariable("id")String id,@ModelAttribute Newuser newuser, Model model) throws IOException {
        System.out.println(newuser.getUuid());
        URL url = new URL("https://qa2.sunbasedata.com/sunbase/portal/api/assignment.jsp?cmd=update&uuid=" + id );
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "Bearer " + token_key);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        if (newuser.getFirst_name() == ""){
            newuser.setFirst_name(null);
        }
        if (newuser.getLast_name() == ""){
            newuser.setLast_name(null);
        }
        String jsonInputString = new Gson().toJson(newuser);
        System.out.println(jsonInputString);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        int status = con.getResponseCode();
        System.out.println(con.getResponseCode());
        if (status == 200) {
            System.out.println(status);
            model.addAttribute("message", "User Updated use list option to see the user");
            model.addAttribute("jsondata", new ArrayList<>());
            return "main";
        } else if (status == 400) {
            model.addAttribute("message", "Either the first name of the last name is null Try again");
            return "regester";
        }
        else if (status == 500){
            model.addAttribute("message", "UUID did not match");
            return "regester";
        }
        else {
            model.addAttribute("message", "Something went wrong try again");
            model.addAttribute("jsondata", new ArrayList<>());
            return "main";
        }


    }

}
