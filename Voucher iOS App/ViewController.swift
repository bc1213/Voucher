//
//  ViewController.swift
//  Voucher iOS App
//
//  Created by Rizwan Sattar on 11/7/15.
//  Copyright © 2015 Rizwan Sattar. All rights reserved.
//

import UIKit
import Voucher

class ViewController: UIViewController, VoucherServerDelegate {

    var server: VoucherServer?

    @IBOutlet var serverStatusLabel: UILabel!
    @IBOutlet var connectionStatusLabel: UILabel!
    @IBOutlet var inputTextFiled: UITextField!
    @IBOutlet var buttonStart: UIButton!
    @IBOutlet var viewController: UIViewController!
    
    var indicator: UIActivityIndicatorView = UIActivityIndicatorView(activityIndicatorStyle: UIActivityIndicatorViewStyle.gray);
   

    deinit {
        self.server?.stop()
        self.server?.delegate = nil
        self.server = nil
    }

    override func viewDidLoad() {
        super.viewDidLoad()
//        let uniqueId = "VoucherTest"
//        self.server = VoucherServer(uniqueSharedId: uniqueId)
//        self.server?.delegate = self
        self.viewController = self
        
        indicator.frame = CGRect(x: 0, y: 0, width: 40, height: 40)
        indicator.center = view.center
        self.view.addSubview(indicator)
        self.view.bringSubview(toFront: indicator)
        UIApplication.shared.isNetworkActivityIndicatorVisible = true
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)

        self.server?.startAdvertising { (displayName, responseHandler) -> Void in

            let alertController = UIAlertController(title: "Allow Auth?", message: "Allow \"\(displayName)\" access to your login?", preferredStyle: .alert)
            alertController.addAction(UIAlertAction(title: "Not Now", style: .cancel, handler: { action in
                responseHandler(nil, nil)
            }))

            alertController.addAction(UIAlertAction(title: "Allow", style: .default, handler: { action in
                // For our authData, use a token string (to simulate an OAuth token, for example)
                let authData = "THIS IS AN AUTH TOKEN".data(using: String.Encoding.utf8)!
                responseHandler(authData, nil)
            }))
            
            let dictionaryToSend = ["deviceID":"IOS|ANANJAN1256","secretToken":"somethingToken","userid":"User_1212"]

             struct dataToPass: Codable {
                var deviceID: String
                var secretToken: String
                var userid: String
            }

            let dog = dataToPass(deviceID:  "IOS|ANANJAN1256", secretToken: "somethingToken", userid: "User_1212")
            let authData = dog.convertToString!.data(using: String.Encoding.utf8)!
            responseHandler(authData,nil)

            self.present(alertController, animated: true, completion: nil)
            
        }
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)

        self.server?.stop()
    }

    // MARK: - VoucherServerDelegate

    func voucherServer(_ server: VoucherServer, didUpdateAdvertising isAdvertising: Bool) {
        var text = "❌ Server Offline."
        if (isAdvertising) {
            text = "✅ Server Online."
        }
        self.serverStatusLabel.text = text
        self.connectionStatusLabel.isHidden = !isAdvertising
    }

    func voucherServer(_ server: VoucherServer, didUpdateConnectionToClient isConnectedToClient: Bool) {
        var text = "📡 Waiting for Connection..."
        if (isConnectedToClient) {
            text = "✅ Connected."
        }
        self.connectionStatusLabel.text = text
    }
    
    @IBAction func onStartButton(_ sender: UIButton) {
        if(inputTextFiled.text?.count ?? 0 < 4) {
            NSLog("Length is less");
            Toast.show(message:"Enter 4 digit code shown on the Voucher tvOS app",controller: viewController);
            return;
        }else{
            let uniqueId = String(inputTextFiled.text!)+"VoucherTest"
            self.server = VoucherServer(uniqueSharedId: uniqueId)
            self.server?.delegate = self
            self.startServer()
            indicator.startAnimating()
        }
    }
    
    func startServer(){
        self.server?.startAdvertising { (displayName, responseHandler) -> Void in

            let alertController = UIAlertController(title: "Allow Auth?", message: "Allow \"\(displayName)\" access to your login?", preferredStyle: .alert)
            alertController.addAction(UIAlertAction(title: "Not Now", style: .cancel, handler: { action in
                responseHandler(nil, nil)
            }))

            alertController.addAction(UIAlertAction(title: "Allow", style: .default, handler: { action in
                // For our authData, use a token string (to simulate an OAuth token, for example)
                let authData = "THIS IS AN AUTH TOKEN".data(using: String.Encoding.utf8)!
                responseHandler(authData, nil)
            }))
            
            let dictionaryToSend = ["deviceID":"IOS|ANANJAN1256","secretToken":"somethingToken","userid":"User_1212"]

             struct dataToPass: Codable {
                var deviceID: String
                var secretToken: String
                var userid: String
            }

            let dog = dataToPass(deviceID:  "IOS|ANANJAN1256", secretToken: "somethingToken", userid: "User_1212")
                            
            let authData = dog.convertToString!.data(using: String.Encoding.utf8)!
            responseHandler(authData,nil)

            self.present(alertController, animated: true, completion: nil)
            
        }
    }

}



extension Encodable {
    var convertToString: String? {
        let jsonEncoder = JSONEncoder()
        jsonEncoder.outputFormatting = .prettyPrinted
        do {
            let jsonData = try jsonEncoder.encode(self)
            return String(data: jsonData, encoding: .utf8)
        } catch {
            return nil
        }
    }
}



class Toast {
    static func show(message: String, controller: UIViewController) {
        let toastContainer = UIView(frame: CGRect())
        toastContainer.backgroundColor = UIColor.black.withAlphaComponent(0.6)
        toastContainer.alpha = 0.0
        toastContainer.layer.cornerRadius = 25;
        toastContainer.clipsToBounds  =  true

        let toastLabel = UILabel(frame: CGRect())
        toastLabel.textColor = UIColor.white
        toastLabel.textAlignment = .center;
        toastLabel.font.withSize(12.0)
        toastLabel.text = message
        toastLabel.clipsToBounds  =  true
        toastLabel.numberOfLines = 0

        toastContainer.addSubview(toastLabel)
        controller.view.addSubview(toastContainer)

        toastLabel.translatesAutoresizingMaskIntoConstraints = false
        toastContainer.translatesAutoresizingMaskIntoConstraints = false

        let a1 = NSLayoutConstraint(item: toastLabel, attribute: .leading, relatedBy: .equal, toItem: toastContainer, attribute: .leading, multiplier: 1, constant: 15)
        let a2 = NSLayoutConstraint(item: toastLabel, attribute: .trailing, relatedBy: .equal, toItem: toastContainer, attribute: .trailing, multiplier: 1, constant: -15)
        let a3 = NSLayoutConstraint(item: toastLabel, attribute: .bottom, relatedBy: .equal, toItem: toastContainer, attribute: .bottom, multiplier: 1, constant: -15)
        let a4 = NSLayoutConstraint(item: toastLabel, attribute: .top, relatedBy: .equal, toItem: toastContainer, attribute: .top, multiplier: 1, constant: 15)
        toastContainer.addConstraints([a1, a2, a3, a4])

        let c1 = NSLayoutConstraint(item: toastContainer, attribute: .leading, relatedBy: .equal, toItem: controller.view, attribute: .leading, multiplier: 1, constant: 65)
        let c2 = NSLayoutConstraint(item: toastContainer, attribute: .trailing, relatedBy: .equal, toItem: controller.view, attribute: .trailing, multiplier: 1, constant: -65)
        let c3 = NSLayoutConstraint(item: toastContainer, attribute: .bottom, relatedBy: .equal, toItem: controller.view, attribute: .bottom, multiplier: 1, constant: -75)
        controller.view.addConstraints([c1, c2, c3])

        UIView.animate(withDuration: 0.5, delay: 0.0, options: .curveEaseIn, animations: {
            toastContainer.alpha = 1.0
        }, completion: { _ in
            UIView.animate(withDuration: 0.5, delay: 1.5, options: .curveEaseOut, animations: {
                toastContainer.alpha = 0.0
            }, completion: {_ in
                toastContainer.removeFromSuperview()
            })
        })
    }
}
