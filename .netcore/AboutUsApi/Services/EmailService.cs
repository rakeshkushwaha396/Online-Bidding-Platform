using System.Net.Mail;
using System.Net;
using AboutUsApi.Models;

namespace AboutUsApi.Services
{
    public class EmailService
    {
        private readonly IConfiguration _config;
        private readonly string adminEmail;

        public EmailService(IConfiguration config)
        {
            _config = config;
            adminEmail = _config["AdminEmail"];
        }

        public async Task SendEmailAsync(AboutUsForm form)
        {
            var message = new MailMessage();
            message.From = new MailAddress(_config["EmailSettings:SenderEmail"]);
            message.To.Add(adminEmail);

            // 👇 Subject ko unique banaya, taaki naye thread me aaye
            message.Subject = $"New About Us Form Submission - {DateTime.Now:yyyyMMdd_HHmmss}";

            message.Body = $"Time: {DateTime.Now}\n\n" +
                           $"Username: {form.Username}\n" +
                           $"Mobile: {form.MobileNumber}\n" +
                           $"Email: {form.Email}\n" +
                           $"Description: {form.Description}";

            using var smtpClient = new SmtpClient(_config["EmailSettings:SmtpServer"], int.Parse(_config["EmailSettings:Port"]))
            {
                Credentials = new NetworkCredential(
                    _config["EmailSettings:SenderEmail"],
                    _config["EmailSettings:SenderPassword"]
                ),
                EnableSsl = true
            };

            await smtpClient.SendMailAsync(message);
        }

    }
}
