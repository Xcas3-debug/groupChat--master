package main.controller;
import main.dto.MessageDTO;
import main.dto.MessageMapper;
import main.dto.UserDTO;
import main.dto.UserMapper;
import main.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@RestController
public class ChatController {//отправл ответ получают запрос браузера
    @Autowired
    UserRepository userRepository;
    @Autowired
    MessageRepository messageRepository;
    @GetMapping("/init")
    public Map<String, Boolean> init() {
        String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
        User user = userRepository.findBySessionId(sessionId).orElse(null);
        return Map.of("result", user != null);
    }
    @PostMapping("/auth")
    public Map<String, Object> auth(@RequestParam String name) {

        Map<String, Object> response = new HashMap<>();
        if (name.isBlank()) {
            response.put("result", false);
           // response.put("message", "Имя не должно быть пустым");
        } else if (userRepository.existsByName(name)) {
            response.put("result", false);
            response.put("message", "Пользователь с таким именем уже существует");
        } else {
            String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
            User user = new User(sessionId, name);
            userRepository.save(user);
            response.put("result", true);
        }
        return response;
    }
    @PostMapping("/messages")
    public  Map<String, Boolean> sendMessage(@RequestParam String message) {
        String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
        User user = userRepository.findBySessionId(sessionId).orElse(null);
        if(user == null || message.isBlank()){
            return Map.of("result", false);
        }
        messageRepository.save(new Message(user, LocalDateTime.now(), message));
        return Map.of("result", true);
    }
    @GetMapping("/messages")
    public List<MessageDTO> getMessagesList() {
        return messageRepository.findAll(Sort.by("dateTime")).stream()
                .map(MessageMapper::map)
                .collect(Collectors.toList());
    }
    @GetMapping("/users")
    public List<UserDTO> getUsersList(){
        return userRepository.findAll(Sort.by("name")).stream()
                .map(UserMapper::map)
                .collect(Collectors.toList());
    }
}
